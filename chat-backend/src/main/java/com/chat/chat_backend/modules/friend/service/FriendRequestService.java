package com.chat.chat_backend.modules.friend.service;

import com.chat.chat_backend.modules.friend.dto.request.HandleFriendRequest;
import com.chat.chat_backend.modules.friend.dto.request.SendFriendRequest;
import com.chat.chat_backend.modules.friend.dto.response.FriendRequestVO;
import java.util.List;

/**
 * 好友申请服务
 */
public interface FriendRequestService {

    void sendFriendRequest(Long currentUserId, SendFriendRequest request);

    List<FriendRequestVO> getFriendRequests(Long currentUserId);

    void handleFriendRequest(Long currentUserId, Long requestId, HandleFriendRequest request);
}