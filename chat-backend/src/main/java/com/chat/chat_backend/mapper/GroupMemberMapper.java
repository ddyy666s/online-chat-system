package com.chat.chat_backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chat.chat_backend.module.entity.GroupMember;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import java.util.List;

@Mapper
public interface GroupMemberMapper extends BaseMapper<GroupMember> {

    @Select("SELECT * FROM group_member WHERE group_id = #{groupId}")
    List<GroupMember> findByGroupId(Long groupId);

    @Select("SELECT * FROM group_member WHERE user_id = #{userId}")
    List<GroupMember> findByUserId(Long userId);

    @Update("UPDATE group_member SET unread_count = unread_count + 1 WHERE group_id = #{groupId} AND user_id != #{fromUserId}")
    void incrementUnreadCount(@Param("groupId") Long groupId, @Param("fromUserId") Long fromUserId);

    @Update("UPDATE group_member SET unread_count = 0, last_read_time = NOW() " +
            "WHERE group_id = #{groupId} AND user_id = #{userId}")
    void clearUnreadCount(@Param("groupId") Long groupId, @Param("userId") Long userId);
}
