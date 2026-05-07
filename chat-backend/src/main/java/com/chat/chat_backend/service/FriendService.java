package com.chat.chat_backend.service;

import com.chat.chat_backend.module.dto.request.HandleFriendRequest;
import com.chat.chat_backend.module.dto.request.MoveFriendGroupRequest;
import com.chat.chat_backend.module.dto.request.SendFriendRequest;
import com.chat.chat_backend.module.dto.response.FriendGroupVO;
import com.chat.chat_backend.module.dto.response.FriendRequestVO;
import com.chat.chat_backend.module.dto.response.FriendVO;
import java.util.List;

public interface FriendService {

    // 搜索用户
    List<FriendVO> searchUsers(Long currentUserId, String keyword);

    // 发送好友申请
    void sendFriendRequest(Long currentUserId, SendFriendRequest request);

    // 获取好友申请列表
    List<FriendRequestVO> getFriendRequests(Long currentUserId);

    // 处理好友申请
    void handleFriendRequest(Long currentUserId, Long requestId, HandleFriendRequest request);

    // 获取好友列表（按分组）
    List<FriendGroupVO> getFriendList(Long currentUserId);

    // 删除好友
    void deleteFriend(Long currentUserId, Long friendId);

    // 移动好友分组
    void moveFriendGroup(Long currentUserId, Long friendId, MoveFriendGroupRequest request);

    // 修改好友备注
    void updateFriendRemark(Long currentUserId, Long friendId, String remark);
}