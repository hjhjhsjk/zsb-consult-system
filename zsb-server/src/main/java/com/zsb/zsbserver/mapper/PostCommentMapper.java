package com.zsb.zsbserver.mapper;

import com.zsb.zsbserver.entity.PostComment;
import com.zsb.zsbserver.vo.CommentVO;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface PostCommentMapper {

    /**
     * 评论列表：昵称来自 user 表，is_liked 用子查询算（uid=0 时自然为 0）。
     * 时间按升序：论坛习惯「先发的在上面」。
     */
    @Select("SELECT c.id, c.user_id, c.content, c.like_count, c.create_time, u.nickname, u.avatar, " +
            "  (SELECT COUNT(*) FROM comment_like cl WHERE cl.comment_id = c.id AND cl.user_id = #{uid}) AS is_liked " +
            "FROM post_comment c JOIN user u ON u.id = c.user_id " +
            "WHERE c.post_id = #{postId} AND c.status = 1 ORDER BY c.create_time ASC")
    List<CommentVO> findByPostId(@Param("postId") Long postId, @Param("uid") Long uid);

    @Insert("INSERT INTO post_comment(post_id, user_id, content) VALUES(#{postId}, #{userId}, #{content})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(PostComment comment);

    @Select("SELECT COUNT(*) FROM post_comment WHERE post_id = #{postId} AND status = 1")
    int countByPostId(@Param("postId") Long postId);

    @Select("SELECT COUNT(*) FROM post_comment WHERE id = #{id} AND status = 1")
    int existsById(@Param("id") Long id);

    @Update("UPDATE post_comment SET like_count = #{count} WHERE id = #{id}")
    void updateLikeCount(@Param("id") Long id, @Param("count") int count);
}
