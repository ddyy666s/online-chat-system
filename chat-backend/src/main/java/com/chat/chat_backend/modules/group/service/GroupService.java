package com.chat.chat_backend.modules.group.service;

import com.chat.chat_backend.modules.group.dto.request.CreateGroupRequest;
import com.chat.chat_backend.modules.group.dto.request.InviteMemberRequest;
import com.chat.chat_backend.modules.group.dto.response.GroupVO;
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

    // 设置管理员（仅群主）
    void setAdmin(Long userId, Long groupId, Long memberId);

    // 取消管理员（仅群主）
    void removeAdmin(Long userId, Long groupId, Long memberId);

    // 移除成员（群主/管理员）
    void removeMember(Long userId, Long groupId, Long memberId);

    // 禁言成员（群主/管理员）
    void muteMember(Long userId, Long groupId, Long memberId, Integer minutes);

    // 取消禁言（群主/管理员）
    void unmuteMember(Long userId, Long groupId, Long memberId);

    // 批量禁言（群主/管理员）
    void batchMute(Long userId, Long groupId, List<Long> memberIds, Integer minutes);
}