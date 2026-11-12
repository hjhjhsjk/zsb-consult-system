package com.zsb.zsbserver.mapper;

import org.apache.ibatis.annotations.*;

/**
 * 帖子点赞。
 * INSERT IGNORE 依赖表上的唯一索引 uk_user_post(user_id, post_id)：
 * 重复点赞不会报错，也不会多出记录 —— 这就是「幂等」的实现方式。
 */
@Mapper
public interface PostLikeMapper {

    @Insert("INSERT IGNORE INTO post_like(user_id, post_id) VALUES(#{uid}, #{postId})")
    void insertIgnore(@Param("uid") Long uid, @Param("postId") Long postId);

    @Delete("DELETE FROM post_like WHERE user_id = #{uid} AND post_id = #{postId}")
    void delete(@Param("uid") Long uid, @Param("postId") Long postId);

    @Select("SELECT COUNT(*) FROM post_like WHERE post_id = #{postId}")
    int countByPostId(@Param("postId") Long postId);
}
