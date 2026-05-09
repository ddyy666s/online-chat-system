package com.chat.chat_backend.service.friend;

import com.chat.chat_backend.module.dto.request.MoveFriendGroupRequest;
import com.chat.chat_backend.module.dto.response.FriendGroupVO;
import com.chat.chat_backend.module.dto.response.FriendVO;
import java.util.List;

/**
 * 好友关系服务
 */
public interface FriendRelationService {

    List<FriendVO> searchUsers(Long currentUserId, String keyword);

    List<FriendGroupVO> getFriendList(Long currentUserId);

    void deleteFriend(Long currentUserId, Long friendId);

    void moveFriendGroup(Long currentUserId, Long friendId, MoveFriendGroupRequest request);

    void updateFriendRemark(Long currentUserId, Long friendId, String remark);
}