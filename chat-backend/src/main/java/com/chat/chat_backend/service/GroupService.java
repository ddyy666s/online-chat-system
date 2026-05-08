package com.chat.chat_backend.service;

import com.chat.chat_backend.module.dto.request.CreateGroupRequest;
import com.chat.chat_backend.module.dto.request.InviteMemberRequest;
import com.chat.chat_backend.module.dto.response.GroupVO;
import java.util.List;

public interface GroupService {
    
    // 创建群聊
    GroupVO createGroup(Long userId, CreateGroupRequest request);
    
    // 获取用户的群聊列表
    List<GroupVO> getUserGroups(Long userId);
    
    // 获取群详情
    GroupVO getGroupDetail(Long userId, Long groupId);
    
    // 邀请成员
    void inviteMember(Long userId, InviteMemberRequest request);
    
    // 退出群聊
    void quitGroup(Long userId, Long groupId);
    
    // 解散群聊（仅群主）
    void disbandGroup(Long userId, Long groupId);

    void clearUnreadCount(Long userId, Long groupId);

    void updateNotice(Long userId, Long groupId, String notice);
}