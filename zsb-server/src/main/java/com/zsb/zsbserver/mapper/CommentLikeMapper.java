package com.zsb.zsbserver.mapper;

import org.apache.ibatis.annotations.*;

/** 评论点赞，幂等原理同 PostLikeMapper（唯一索引 uk_user_comment） */
@Mapper
public interface CommentLikeMapper {

    @Insert("INSERT IGNORE INTO comment_like(user_id, comment_id) VALUES(#{uid}, #{commentId})")
    void insertIgnore(@Param("uid") Long uid, @Param("commentId") Long commentId);

    @Delete("DELETE FROM comment_like WHERE user_id = #{uid} AND comment_id = #{commentId}")
    void delete(@Param("uid") Long uid, @Param("commentId") Long commentId);

    @Select("SELECT COUNT(*) FROM comment_like WHERE comment_id = #{commentId}")
    int countByCommentId(@Param("commentId") Long commentId);
}
