package com.chat.chat_backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chat.chat_backend.module.entity.Message;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
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

    @Select("SELECT from_user_id as fromUserId, COUNT(*) as count FROM message " +
            "WHERE to_user_id = #{userId} AND is_read = 0 AND recall_time IS NULL " +
            "AND from_user_id IS NOT NULL " +
            "GROUP BY from_user_id")
    List<UnreadGroup> groupUnreadByFriend(@Param("userId") Long userId);

    // 新增：查询未读消息详情（用于消息盒子）
    @Select("SELECT m.id, m.from_user_id as fromUserId, m.content, m.send_time as sendTime, " +
            "u.nickname as fromUserNickname, u.avatar as fromUserAvatar " +
            "FROM message m " +
            "LEFT JOIN user u ON m.from_user_id = u.id " +
            "WHERE m.to_user_id = #{userId} AND m.is_read = 0 AND m.recall_time IS NULL " +
            "ORDER BY m.send_time DESC")
    List<UnreadMessageDetail> findUnreadMessages(@Param("userId") Long userId);

    @Update("UPDATE message SET is_read = 1, read_time = NOW() " +
            "WHERE to_user_id = #{userId} AND from_user_id = #{friendId} AND is_read = 0")
    Integer markAsRead(@Param("userId") Long userId, @Param("friendId") Long friendId);

    @Update("UPDATE message SET recall_time = NOW() " +
            "WHERE id = #{messageId} AND from_user_id = #{userId} " +
            "AND send_time > DATE_SUB(NOW(), INTERVAL 2 MINUTE)")
    Integer recallMessage(@Param("messageId") Long messageId, @Param("userId") Long userId);

    // 内部类：未读消息分组
    class UnreadGroup {
        private Long fromUserId;
        private Integer count;

        public Long getFromUserId() { return fromUserId; }
        public void setFromUserId(Long fromUserId) { this.fromUserId = fromUserId; }
        public Integer getCount() { return count; }
        public void setCount(Integer count) { this.count = count; }
    }

    // 内部类：未读消息详情
    class UnreadMessageDetail {
        private Long id;
        private Long fromUserId;
        private String fromUserNickname;
        private String fromUserAvatar;
        private String content;
        private LocalDateTime sendTime;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Long getFromUserId() { return fromUserId; }
        public void setFromUserId(Long fromUserId) { this.fromUserId = fromUserId; }
        public String getFromUserNickname() { return fromUserNickname; }
        public void setFromUserNickname(String fromUserNickname) { this.fromUserNickname = fromUserNickname; }
        public String getFromUserAvatar() { return fromUserAvatar; }
        public void setFromUserAvatar(String fromUserAvatar) { this.fromUserAvatar = fromUserAvatar; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public LocalDateTime getSendTime() { return sendTime; }
        public void setSendTime(LocalDateTime sendTime) { this.sendTime = sendTime; }
    }
}