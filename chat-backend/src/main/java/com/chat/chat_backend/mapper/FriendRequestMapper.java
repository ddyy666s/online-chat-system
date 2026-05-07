package com.chat.chat_backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chat.chat_backend.module.entity.FriendRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import java.util.List;

@Mapper
public interface FriendRequestMapper extends BaseMapper<FriendRequest> {

    @Select("SELECT * FROM friend_request WHERE to_user_id = #{toUserId} AND status = 0 ORDER BY created_at DESC")
    List<FriendRequest> findPendingRequests(@Param("toUserId") Long toUserId);

    @Select("SELECT * FROM friend_request WHERE from_user_id = #{fromUserId} AND to_user_id = #{toUserId} AND status = 0")
    FriendRequest findPendingRequest(@Param("fromUserId") Long fromUserId, @Param("toUserId") Long toUserId);

    @Update("UPDATE friend_request SET status = 3, expire_time = NOW() WHERE status = 0 AND expire_time < NOW()")
    void expireOldRequests();
}