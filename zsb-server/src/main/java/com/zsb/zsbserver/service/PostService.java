package com.zsb.zsbserver.service;

import com.zsb.zsbserver.entity.Post;
import com.zsb.zsbserver.vo.CommentVO;
import com.zsb.zsbserver.vo.FavoriteItemVO;
import com.zsb.zsbserver.vo.PostDetailVO;

import java.util.List;
import java.util.Map;

public interface PostService {

    void addPost(Post post);

    List<Post> getAllPosts();

    /** 管理端删帖：软删除（status=0） */
    void deletePost(Long id);

    List<Post> getPosts(Integer regionId, Integer schoolId);

    /** 首页最新讨论：跨省份取最新 N 条 */
    List<Post> getLatestPosts(int limit);

    /** 帖子详情（含作者昵称、点赞/收藏状态，并把浏览量 +1） */
    PostDetailVO getDetail(Long uid, Long postId);

    List<CommentVO> getComments(Long uid, Long postId);

    void addComment(Long uid, Long postId, String content);

    /** @param like true=点赞 false=取消；返回最新计数与状态 */
    Map<String, Object> likePost(Long uid, Long postId, boolean like);

    Map<String, Object> likeComment(Long uid, Long commentId, boolean like);

    /** @param add true=收藏 false=取消收藏；targetType 1=帖子 2=政策 */
    Map<String, Object> favorite(Long uid, Integer targetType, Long targetId, boolean add);

    List<FavoriteItemVO> getFavorites(Long uid, Integer type);

    List<Post> getMyPosts(Long uid);
}
