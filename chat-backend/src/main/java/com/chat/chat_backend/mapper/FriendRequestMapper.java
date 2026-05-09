package com.chat.chat_backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chat.chat_backend.module.entity.FriendRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface FriendRequestMapper extends BaseMapper<FriendRequest> {
    List<FriendRequest> findPendingRequests(@Param("toUserId") Long toUserId);
    FriendRequest findPendingRequest(@Param("fromUserId") Long fromUserId, @Param("toUserId") Long toUserId);
    void expireOldRequests();
}