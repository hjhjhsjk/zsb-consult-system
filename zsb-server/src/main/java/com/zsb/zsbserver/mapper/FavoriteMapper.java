package com.zsb.zsbserver.mapper;

import com.zsb.zsbserver.vo.FavoriteItemVO;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface FavoriteMapper {

    @Insert("INSERT IGNORE INTO favorite(user_id, target_type, target_id) " +
            "VALUES(#{uid}, #{targetType}, #{targetId})")
    void insertIgnore(@Param("uid") Long uid,
                      @Param("targetType") Integer targetType,
                      @Param("targetId") Long targetId);

    @Delete("DELETE FROM favorite WHERE user_id = #{uid} AND target_type = #{targetType} " +
            "AND target_id = #{targetId}")
    void delete(@Param("uid") Long uid,
                @Param("targetType") Integer targetType,
                @Param("targetId") Long targetId);

    @Select("SELECT COUNT(*) FROM favorite WHERE target_type = 1 AND target_id = #{postId}")
    int countPostFavorites(@Param("postId") Long postId);

    /** 收藏的帖子：id 返回 target_id（前端要拿它跳详情） */
    @Select("SELECT f.target_id AS id, f.target_id, f.target_type, p.content, p.image_url, p.post_time " +
            "FROM favorite f JOIN post p ON p.id = f.target_id " +
            "WHERE f.user_id = #{uid} AND f.target_type = 1 AND p.status = 1 " +
            "ORDER BY f.create_time DESC")
    List<FavoriteItemVO> findPostFavorites(@Param("uid") Long uid);

    /** 收藏的政策：title / create_time 给前端兜底展示 */
    @Select("SELECT f.target_id AS id, f.target_id, f.target_type, pol.title, pol.create_time " +
            "FROM favorite f JOIN policy pol ON pol.id = f.target_id " +
            "WHERE f.user_id = #{uid} AND f.target_type = 2 " +
            "ORDER BY f.create_time DESC")
    List<FavoriteItemVO> findPolicyFavorites(@Param("uid") Long uid);
}
