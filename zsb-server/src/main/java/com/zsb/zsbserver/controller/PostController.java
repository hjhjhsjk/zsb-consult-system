package com.zsb.zsbserver.controller;

import com.zsb.zsbserver.common.AuthInterceptor;
import com.zsb.zsbserver.common.ForbiddenException;
import com.zsb.zsbserver.common.Result;
import com.zsb.zsbserver.dto.CommentDTO;
import com.zsb.zsbserver.entity.Post;
import com.zsb.zsbserver.service.FileStorageService;
import com.zsb.zsbserver.service.PostService;
import com.zsb.zsbserver.vo.CommentVO;
import com.zsb.zsbserver.vo.FavoriteItemVO;
import com.zsb.zsbserver.vo.PostDetailVO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 论坛模块。
 *
 * 注意两类 uid：
 *   1. 路径里的 {uid} —— 前端历史遗留，用来做「我是谁」的入参
 *   2. token 里的 uid  —— 真正可信的身份
 * 所以每个写操作都要校验「路径 uid == token uid」，否则改个数字就能替别人点赞。
 */
@RestController
@RequestMapping("/BBS")
public class PostController {

    @Autowired
    private PostService postService;

    @Autowired
    private FileStorageService fileStorageService;

    /** G-01 帖子列表（免登录）：schoolId=0 表示全省帖 */
    @GetMapping("/info/{provinceId}/{schoolId}")
    public Result<List<Post>> getPosts(@PathVariable Integer provinceId, @PathVariable Integer schoolId) {
        return Result.success(postService.getPosts(provinceId, schoolId));
    }

    /**
     * 首页「最新讨论」（免登录）：跨省份按时间倒序取 N 条。
     * limit 做了上限保护，避免前端传 10000 把库拖死。
     */
    @GetMapping("/latest")
    public Result<List<Post>> getLatestPosts(@RequestParam(defaultValue = "6") Integer limit) {
        int size = Math.min(Math.max(limit, 1), 20);
        return Result.success(postService.getLatestPosts(size));
    }

    /**
     * G-03 发帖（multipart/form-data）
     * 前端字段：content、user_id、region_id、school_id、image（可选）
     * 图片存本地后把 /uploads/... 写进 image_url。
     */
    @PostMapping("/insert")
    public Result<String> addPost(@RequestParam("content") String content,
                                  @RequestParam("region_id") Integer regionId,
                                  @RequestParam(value = "school_id", defaultValue = "0") Integer schoolId,
                                  @RequestPart(value = "image", required = false) MultipartFile image,
                                  HttpServletRequest request) {
        Post post = new Post();
        // 作者一律取 token 里的 uid，不信前端传的 user_id
        post.setUserId(currentUid(request));
        post.setContent(content);
        post.setRegionId(regionId);
        post.setSchoolId(schoolId);
        post.setImageUrl(fileStorageService.saveImage(image, "post"));
        postService.addPost(post);
        return Result.success("发帖成功");
    }

    /** G-02 帖子详情：uid=0 表示游客/管理端，不带点赞收藏状态 */
    @GetMapping("/GetPostDetail/{uid}/{postId}")
    public Result<PostDetailVO> getPostDetail(@PathVariable Long uid, @PathVariable Long postId,
                                              HttpServletRequest request) {
        Long viewer = resolveViewer(uid, request);
        return Result.success(postService.getDetail(viewer, postId));
    }

    /** G-04 评论列表 */
    @GetMapping("/comments/{uid}/{postId}")
    public Result<List<CommentVO>> getComments(@PathVariable Long uid, @PathVariable Long postId,
                                               HttpServletRequest request) {
        Long viewer = resolveViewer(uid, request);
        return Result.success(postService.getComments(viewer, postId));
    }

    /** M-11 发表评论 */
    @PostMapping("/comments")
    public Result<String> addComment(@RequestBody CommentDTO dto, HttpServletRequest request) {
        if (dto.getPostId() == null) {
            throw new IllegalArgumentException("缺少参数 post_id");
        }
        postService.addComment(currentUid(request), dto.getPostId(), dto.getContent());
        return Result.success("评论成功");
    }

    /** H-01 点赞帖子 */
    @PostMapping("/like_post/{uid}/{postId}")
    public Result<Map<String, Object>> likePost(@PathVariable Long uid, @PathVariable Long postId,
                                                HttpServletRequest request) {
        checkSelf(uid, request);
        return Result.success(postService.likePost(uid, postId, true));
    }

    /** H-02 取消点赞帖子 */
    @PostMapping("/unlike_post/{uid}/{postId}")
    public Result<Map<String, Object>> unlikePost(@PathVariable Long uid, @PathVariable Long postId,
                                                  HttpServletRequest request) {
        checkSelf(uid, request);
        return Result.success(postService.likePost(uid, postId, false));
    }

    /** H-03 点赞评论 */
    @PostMapping("/like_comment/{uid}/{commentId}")
    public Result<Map<String, Object>> likeComment(@PathVariable Long uid, @PathVariable Long commentId,
                                                   HttpServletRequest request) {
        checkSelf(uid, request);
        return Result.success(postService.likeComment(uid, commentId, true));
    }

    /** H-04 取消点赞评论 */
    @PostMapping("/unlike_comment/{uid}/{commentId}")
    public Result<Map<String, Object>> unlikeComment(@PathVariable Long uid, @PathVariable Long commentId,
                                                     HttpServletRequest request) {
        checkSelf(uid, request);
        return Result.success(postService.likeComment(uid, commentId, false));
    }

    /** H-05 收藏（targetType：1 帖子 2 政策） */
    @PostMapping("/add_favorite/{uid}/{targetType}/{targetId}")
    public Result<Map<String, Object>> addFavorite(@PathVariable Long uid,
                                                   @PathVariable Integer targetType,
                                                   @PathVariable Long targetId,
                                                   HttpServletRequest request) {
        checkSelf(uid, request);
        return Result.success(postService.favorite(uid, targetType, targetId, true));
    }

    /** H-06 取消收藏 */
    @PostMapping("/clear_favorite/{uid}/{targetType}/{targetId}")
    public Result<Map<String, Object>> clearFavorite(@PathVariable Long uid,
                                                     @PathVariable Integer targetType,
                                                     @PathVariable Long targetId,
                                                     HttpServletRequest request) {
        checkSelf(uid, request);
        return Result.success(postService.favorite(uid, targetType, targetId, false));
    }

    /** H-07 我的收藏（type：1 帖子 2 政策）。路径 favfavorite 是前端历史拼写，别改 */
    @GetMapping("/get_favfavorite/{uid}/{type}")
    public Result<List<FavoriteItemVO>> getFavorites(@PathVariable Long uid, @PathVariable Integer type,
                                                     HttpServletRequest request) {
        checkSelf(uid, request);
        return Result.success(postService.getFavorites(uid, type));
    }

    /** M-6 我的发帖 */
    @GetMapping("/myPosts")
    public Result<List<Post>> myPosts(HttpServletRequest request) {
        return Result.success(postService.getMyPosts(currentUid(request)));
    }

    // ================= 身份校验工具 =================

    private Long currentUid(HttpServletRequest request) {
        Long uid = (Long) request.getAttribute(AuthInterceptor.ATTR_UID);
        if (uid == null) {
            throw new ForbiddenException("登录状态无效");
        }
        return uid;
    }

    /** 写操作：路径 uid 必须等于 token uid */
    private void checkSelf(Long pathUid, HttpServletRequest request) {
        if (pathUid == null || !pathUid.equals(currentUid(request))) {
            throw new ForbiddenException("只能操作自己的账号");
        }
    }

    /** 读操作：uid=0 视为游客，否则也只能看自己的点赞状态 */
    private Long resolveViewer(Long pathUid, HttpServletRequest request) {
        if (pathUid == null || pathUid == 0L) {
            return 0L;
        }
        Long tokenUid = (Long) request.getAttribute(AuthInterceptor.ATTR_UID);
        if (tokenUid == null) {
            return 0L;
        }
        if (!tokenUid.equals(pathUid)) {
            throw new ForbiddenException("只能查看自己的数据");
        }
        return pathUid;
    }
}
