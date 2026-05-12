package com.chat.chat_backend.modules.group.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chat.chat_backend.modules.group.entity.GroupMember;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface GroupMemberMapper extends BaseMapper<GroupMember> {
    List<GroupMember> findByGroupId(@Param("groupId") Long groupId);
    List<GroupMember> findByUserId(@Param("userId") Long userId);
    void incrementUnreadCount(@Param("groupId") Long groupId, @Param("fromUserId") Long fromUserId);
    void clearUnreadCount(@Param("groupId") Long groupId, @Param("userId") Long userId);
}