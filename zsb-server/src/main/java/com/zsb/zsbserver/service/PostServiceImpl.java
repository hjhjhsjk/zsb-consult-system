package com.zsb.zsbserver.service;

import com.zsb.zsbserver.entity.Post;
import com.zsb.zsbserver.entity.PostComment;
import com.zsb.zsbserver.mapper.CommentLikeMapper;
import com.zsb.zsbserver.mapper.FavoriteMapper;
import com.zsb.zsbserver.mapper.PolicyMapper;
import com.zsb.zsbserver.mapper.PostCommentMapper;
import com.zsb.zsbserver.mapper.PostLikeMapper;
import com.zsb.zsbserver.mapper.PostMapper;
import com.zsb.zsbserver.vo.CommentVO;
import com.zsb.zsbserver.vo.FavoriteItemVO;
import com.zsb.zsbserver.vo.PostDetailVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 帖子 + 评论 + 点赞 + 收藏 的业务逻辑。
 *
 * 这里反复出现的一个套路：先改关系表（like/favorite），
 * 再用 COUNT(*) 重新算一遍冗余计数字段。
 * 为什么不用 like_count = like_count + 1 ？
 *   - 重复点赞会被 INSERT IGNORE 吞掉，+1 就会算错；
 *   - 用 COUNT 重算永远和真实数据一致，代价只是一条走索引的 count 查询。
 */
@Service
public class PostServiceImpl implements PostService {

    @Autowired
    private PostMapper postMapper;

    @Autowired
    private PostCommentMapper commentMapper;

    @Autowired
    private PostLikeMapper postLikeMapper;

    @Autowired
    private CommentLikeMapper commentLikeMapper;

    @Autowired
    private FavoriteMapper favoriteMapper;

    @Autowired
    private PolicyMapper policyMapper;

    @Override
    @Transactional
    public void addPost(Post post) {
        if (post.getContent() == null || post.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("帖子内容不能为空");
        }
        String content = post.getContent().trim();
        if (content.length() > 500) {
            throw new IllegalArgumentException("帖子内容最多 500 字");
        }
        if (post.getRegionId() == null) {
            throw new IllegalArgumentException("请选择地区");
        }
        post.setContent(content);
        post.setSchoolId(post.getSchoolId() == null ? 0 : post.getSchoolId());
        post.setImageUrl(post.getImageUrl() == null ? "" : post.getImageUrl());
        postMapper.insert(post);
    }

    @Override
    public List<Post> getAllPosts() {
        return postMapper.findAllPosts();
    }

    @Override
    public void deletePost(Long id) {
        if (postMapper.findDetailById(id, 0L) == null) {
            throw new IllegalArgumentException("帖子不存在或已删除");
        }
        postMapper.softDeleteById(id);
    }

    @Override
    public List<Post> getPosts(Integer regionId, Integer schoolId) {
        return postMapper.findByRegionAndSchool(regionId, schoolId == null ? 0 : schoolId);
    }

    @Override
    public List<Post> getLatestPosts(int limit) {
        return postMapper.findLatest(limit);
    }

    @Override
    public PostDetailVO getDetail(Long uid, Long postId) {
        PostDetailVO vo = postMapper.findDetailById(postId, uid == null ? 0L : uid);
        if (vo == null) {
            throw new IllegalArgumentException("帖子不存在或已删除");
        }
        // 每打开一次详情 +1，返回给前端的就是最新值
        postMapper.increaseViewCount(postId);
        vo.setViewCount((vo.getViewCount() == null ? 0 : vo.getViewCount()) + 1);
        return vo;
    }

    @Override
    public List<CommentVO> getComments(Long uid, Long postId) {
        return commentMapper.findByPostId(postId, uid == null ? 0L : uid);
    }

    @Override
    @Transactional
    public void addComment(Long uid, Long postId, String content) {
        requirePost(postId);
        String text = content == null ? "" : content.trim();
        if (text.isEmpty()) {
            throw new IllegalArgumentException("评论内容不能为空");
        }
        if (text.length() > 500) {
            throw new IllegalArgumentException("评论最多 500 字");
        }

        PostComment comment = new PostComment();
        comment.setPostId(postId);
        comment.setUserId(uid);
        comment.setContent(text);
        commentMapper.insert(comment);

        // 评论数冗余字段同步，列表页就不用每条帖子再 count 一次
        postMapper.updateCommentCount(postId, commentMapper.countByPostId(postId));
    }

    @Override
    @Transactional
    public Map<String, Object> likePost(Long uid, Long postId, boolean like) {
        requirePost(postId);
        if (like) {
            postLikeMapper.insertIgnore(uid, postId);
        } else {
            postLikeMapper.delete(uid, postId);
        }
        int count = postLikeMapper.countByPostId(postId);
        postMapper.updateLikeCount(postId, count);
        return state(count, "like_count", like ? 1 : 0, "is_liked");
    }

    @Override
    @Transactional
    public Map<String, Object> likeComment(Long uid, Long commentId, boolean like) {
        if (commentMapper.existsById(commentId) == 0) {
            throw new IllegalArgumentException("评论不存在或已删除");
        }
        if (like) {
            commentLikeMapper.insertIgnore(uid, commentId);
        } else {
            commentLikeMapper.delete(uid, commentId);
        }
        int count = commentLikeMapper.countByCommentId(commentId);
        commentMapper.updateLikeCount(commentId, count);
        return state(count, "like_count", like ? 1 : 0, "is_liked");
    }

    @Override
    @Transactional
    public Map<String, Object> favorite(Long uid, Integer targetType, Long targetId, boolean add) {
        if (targetType == null || (targetType != 1 && targetType != 2)) {
            throw new IllegalArgumentException("收藏类型不合法，只能是 1（帖子）或 2（政策）");
        }
        // 收藏表是多态的、没有外键，必须自己校验目标是否存在
        if (targetType == 1) {
            requirePost(targetId);
        } else if (policyMapper.findById(targetId, 0L) == null) {
            throw new IllegalArgumentException("政策不存在");
        }

        if (add) {
            favoriteMapper.insertIgnore(uid, targetType, targetId);
        } else {
            favoriteMapper.delete(uid, targetType, targetId);
        }

        if (targetType == 1) {
            int count = favoriteMapper.countPostFavorites(targetId);
            postMapper.updateFavCount(targetId, count);
            return state(count, "fav_count", add ? 1 : 0, "is_fav");
        }
        return state(0, "fav_count", add ? 1 : 0, "is_fav");
    }

    @Override
    public List<FavoriteItemVO> getFavorites(Long uid, Integer type) {
        if (type != null && type == 2) {
            return favoriteMapper.findPolicyFavorites(uid);
        }
        return favoriteMapper.findPostFavorites(uid);
    }

    @Override
    public List<Post> getMyPosts(Long uid) {
        return postMapper.findByUserId(uid);
    }

    private void requirePost(Long postId) {
        if (postMapper.findDetailById(postId, 0L) == null) {
            throw new IllegalArgumentException("帖子不存在或已删除");
        }
    }

    /** 统一拼装 {like_count: n, is_liked: 1} 这种返回结构 */
    private Map<String, Object> state(int count, String countKey, int flag, String flagKey) {
        Map<String, Object> result = new HashMap<>();
        result.put(countKey, count);
        result.put(flagKey, flag);
        return result;
    }
}
