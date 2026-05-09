package com.chat.chat_backend.service.friend;

import com.chat.chat_backend.module.dto.request.HandleFriendRequest;
import com.chat.chat_backend.module.dto.request.SendFriendRequest;
import com.chat.chat_backend.module.dto.response.FriendRequestVO;
import java.util.List;

/**
 * 好友申请服务
 */
public interface FriendRequestService {

    void sendFriendRequest(Long currentUserId, SendFriendRequest request);

    List<FriendRequestVO> getFriendRequests(Long currentUserId);

    void handleFriendRequest(Long currentUserId, Long requestId, HandleFriendRequest request);
}