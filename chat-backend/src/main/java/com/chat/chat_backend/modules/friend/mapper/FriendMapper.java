package com.chat.chat_backend.modules.friend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chat.chat_backend.modules.friend.entity.Friend;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface FriendMapper extends BaseMapper<Friend> {
    Friend findFriendRelation(@Param("userId") Long userId, @Param("friendId") Long friendId);
    List<Friend> findAllByUserId(@Param("userId") Long userId);
}