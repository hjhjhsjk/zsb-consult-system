package com.zsb.zsbserver.mapper;

import com.zsb.zsbserver.entity.Post;
import com.zsb.zsbserver.vo.PostDetailVO;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface PostMapper {

    @Insert("INSERT INTO post(user_id, content, image_url, region_id, school_id) " +
            "VALUES(#{userId}, #{content}, #{imageUrl}, #{regionId}, #{schoolId})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Post post);

    /**
     * 帖子列表：schoolId = 0 表示「全省帖」，要返回该省所有帖子；
     * 传具体学校时只返回该校帖子。所以不能用 school_id = #{schoolId} 死等。
     * 同时过滤 status = 1（软删除的帖子不展示）。
     */
    @Select("SELECT p.*, u.nickname, u.avatar FROM post p JOIN user u ON u.id = p.user_id " +
            "WHERE p.status = 1 AND p.region_id = #{regionId} " +
            "AND (#{schoolId} = 0 OR p.school_id = #{schoolId}) ORDER BY p.post_time DESC")
    List<Post> findByRegionAndSchool(@Param("regionId") Integer regionId,
                                     @Param("schoolId") Integer schoolId);

    @Select("SELECT p.*, u.nickname, u.avatar FROM post p JOIN user u ON u.id = p.user_id " +
            "WHERE p.status = 1 ORDER BY p.post_time DESC")
    List<Post> findAllPosts();

    /** 首页「最新讨论」：跨省份取最新 N 条，不区分地区 */
    @Select("SELECT p.*, u.nickname, u.avatar FROM post p JOIN user u ON u.id = p.user_id " +
            "WHERE p.status = 1 ORDER BY p.post_time DESC LIMIT #{limit}")
    List<Post> findLatest(@Param("limit") int limit);

    @Select("SELECT p.*, u.nickname, u.avatar FROM post p JOIN user u ON u.id = p.user_id " +
            "WHERE p.user_id = #{uid} AND p.status = 1 ORDER BY p.post_time DESC")
    List<Post> findByUserId(@Param("uid") Long uid);

    /**
     * 详情：一次 SQL 把作者昵称、当前用户点赞/收藏状态都带出来，
     * 省掉「查帖子 → 查用户 → 查点赞 → 查收藏」四次往返。
     * uid = 0（游客/管理端）时两个 EXISTS 自然为 0。
     */
    @Select("SELECT p.*, u.nickname, u.avatar, " +
            "  (SELECT COUNT(*) FROM post_like pl WHERE pl.post_id = p.id AND pl.user_id = #{uid}) AS is_liked, " +
            "  (SELECT COUNT(*) FROM favorite f WHERE f.target_type = 1 AND f.target_id = p.id AND f.user_id = #{uid}) AS is_fav " +
            "FROM post p JOIN user u ON u.id = p.user_id " +
            "WHERE p.id = #{postId} AND p.status = 1")
    PostDetailVO findDetailById(@Param("postId") Long postId, @Param("uid") Long uid);

    @Update("UPDATE post SET view_count = view_count + 1 WHERE id = #{id}")
    void increaseViewCount(@Param("id") Long id);

    /** 管理端删帖用软删除，保留数据可回溯 */
    @Update("UPDATE post SET status = 0 WHERE id = #{id}")
    void softDeleteById(@Param("id") Long id);

    @Update("UPDATE post SET like_count = #{count} WHERE id = #{postId}")
    void updateLikeCount(@Param("postId") Long postId, @Param("count") int count);

    @Update("UPDATE post SET fav_count = #{count} WHERE id = #{postId}")
    void updateFavCount(@Param("postId") Long postId, @Param("count") int count);

    @Update("UPDATE post SET post_count = #{count} WHERE id = #{postId}")
    void updateCommentCount(@Param("postId") Long postId, @Param("count") int count);

    @Select("SELECT COUNT(*) FROM post WHERE status = 1")
    int countAll();
}
