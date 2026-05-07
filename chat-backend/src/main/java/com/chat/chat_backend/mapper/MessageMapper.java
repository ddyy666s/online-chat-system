package com.chat.chat_backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chat.chat_backend.module.entity.Message;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import java.util.List;

@Mapper
public interface MessageMapper extends BaseMapper<Message> {

    @Select("SELECT * FROM message WHERE (from_user_id = #{userId} AND to_user_id = #{friendId}) " +
            "OR (from_user_id = #{friendId} AND to_user_id = #{userId}) " +
            "ORDER BY send_time DESC LIMIT #{limit} OFFSET #{offset}")
    List<Message> findChatHistory(@Param("userId") Long userId,
                                  @Param("friendId") Long friendId,
                                  @Param("offset") Integer offset,
                                  @Param("limit") Integer limit);

    @Select("SELECT COUNT(*) FROM message WHERE to_user_id = #{userId} AND is_read = 0 AND recall_time IS NULL")
    Integer countUnreadTotal(@Param("userId") Long userId);

    @Select("SELECT from_user_id, COUNT(*) as count FROM message " +
            "WHERE to_user_id = #{userId} AND is_read = 0 AND recall_time IS NULL " +
            "GROUP BY from_user_id")
    List<UnreadGroup> groupUnreadByFriend(@Param("userId") Long userId);

    @Update("UPDATE message SET is_read = 1, read_time = NOW() " +
            "WHERE to_user_id = #{userId} AND from_user_id = #{friendId} AND is_read = 0")
    Integer markAsRead(@Param("userId") Long userId, @Param("friendId") Long friendId);

    @Update("UPDATE message SET recall_time = NOW() " +
            "WHERE id = #{messageId} AND from_user_id = #{userId} " +
            "AND send_time > DATE_SUB(NOW(), INTERVAL 2 MINUTE)")
    Integer recallMessage(@Param("messageId") Long messageId, @Param("userId") Long userId);

    // 内部类
    class UnreadGroup {
        public Long from_user_id;
        public Integer count;
    }
}