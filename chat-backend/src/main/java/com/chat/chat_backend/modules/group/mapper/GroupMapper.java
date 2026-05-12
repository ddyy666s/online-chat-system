package com.chat.chat_backend.modules.group.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chat.chat_backend.modules.group.entity.Group;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface GroupMapper extends BaseMapper<Group> {
    List<Group> findGroupsByUserId(@Param("userId") Long userId);
    void clearUnreadCount(@Param("groupId") Long groupId, @Param("userId") Long userId);
}