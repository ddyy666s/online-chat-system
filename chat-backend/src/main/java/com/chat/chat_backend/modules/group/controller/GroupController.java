package com.chat.chat_backend.modules.group.controller;

import com.chat.chat_backend.common.result.Result;
import com.chat.chat_backend.modules.group.mapper.GroupMemberMapper;
import com.chat.chat_backend.modules.user.mapper.UserMapper;
import com.chat.chat_backend.modules.group.dto.request.CreateGroupRequest;
import com.chat.chat_backend.modules.group.dto.request.InviteMemberRequest;
import com.chat.chat_backend.modules.group.dto.response.GroupMemberVO;
import com.chat.chat_backend.modules.group.dto.response.GroupVO;
import com.chat.chat_backend.modules.group.entity.GroupMember;
import com.chat.chat_backend.modules.user.entity.User;
import com.chat.chat_backend.modules.group.service.GroupService;
import com.chat.chat_backend.modules.group.service.impl.GroupServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/group")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;
    private final GroupMemberMapper groupMemberMapper;  // 添加
    private final UserMapper userMapper;  // 添加

    /**
     * 创建群聊
     */
    @PostMapping
    public Result<GroupVO> createGroup(HttpServletRequest request, @RequestBody CreateGroupRequest req) {
        Long userId = (Long) request.getAttribute("userId");
        GroupVO result = groupService.createGroup(userId, req);
        return Result.success(result);
    }

    /**
     * 获取用户的群聊列表
     */
    @GetMapping("/list")
    public Result<List<GroupVO>> getUserGroups(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<GroupVO> result = groupService.getUserGroups(userId);
        return Result.success(result);
    }

    /**
     * 获取群详情
     */
    @GetMapping("/{groupId}")
    public Result<GroupVO> getGroupDetail(HttpServletRequest request, @PathVariable Long groupId) {
        Long userId = (Long) request.getAttribute("userId");
        GroupVO result = groupService.getGroupDetail(userId, groupId);
        return Result.success(result);
    }

    /**
     * 邀请成员
     */
    @PostMapping("/invite")
    public Result<Void> inviteMember(HttpServletRequest request, @RequestBody InviteMemberRequest req) {
        Long userId = (Long) request.getAttribute("userId");
        groupService.inviteMember(userId, req);
        return Result.success("邀请成功", null);
    }

    /**
     * 退出群聊
     */
    @DeleteMapping("/{groupId}/quit")
    public Result<Void> quitGroup(HttpServletRequest request, @PathVariable Long groupId) {
        Long userId = (Long) request.getAttribute("userId");
        groupService.quitGroup(userId, groupId);
        return Result.success("已退出群聊", null);
    }

    /**
     * 解散群聊
     */
    @DeleteMapping("/{groupId}/disband")
    public Result<Void> disbandGroup(HttpServletRequest request, @PathVariable Long groupId) {
        Long userId = (Long) request.getAttribute("userId");
        groupService.disbandGroup(userId, groupId);
        return Result.success("群聊已解散", null);
    }

    /**
     * 获取群成员列表
     */
    @GetMapping("/{groupId}/members")
    public Result<List<GroupMemberVO>> getGroupMembers(@PathVariable Long groupId) {
        List<GroupMember> members = groupMemberMapper.findByGroupId(groupId);
        List<GroupMemberVO> result = members.stream().map(member -> {
            User user = userMapper.selectById(member.getUserId());
            return GroupMemberVO.builder()
                    .userId(member.getUserId())
                    .nickname(user != null ? user.getNickname() : "未知用户")
                    .avatar(user != null ? user.getAvatar() : null)
                    .groupNickname(member.getNickname())
                    .role(member.getRole())
                    .muted(GroupServiceImpl.isMuted(groupId, member.getUserId()))
                    .build();
        }).collect(Collectors.toList());
        return Result.success(result);
    }
    /**
     * 清除群未读消息计数
     */
    @PutMapping("/{groupId}/read")
    public Result<Void> clearUnreadCount(HttpServletRequest request, @PathVariable Long groupId) {
        Long userId = (Long) request.getAttribute("userId");
        groupService.clearUnreadCount(userId, groupId);
        return Result.success("已清除未读", null);
    }

    /**
     * 更新群公告（仅群主和管理员）
     */
    @PutMapping("/{groupId}/notice")
    public Result<Void> updateNotice(HttpServletRequest request,
                                     @PathVariable Long groupId,
                                     @RequestBody Map<String, String> body) {
        Long userId = (Long) request.getAttribute("userId");
        String notice = body.get("notice");
        groupService.updateNotice(userId, groupId, notice);
        return Result.success("群公告已更新", null);
    }

    /**
     * 设置管理员（仅群主）
     */
    @PutMapping("/{groupId}/member/{memberId}/set-admin")
    public Result<Void> setAdmin(HttpServletRequest request,
                                 @PathVariable Long groupId,
                                 @PathVariable Long memberId) {
        Long userId = (Long) request.getAttribute("userId");
        groupService.setAdmin(userId, groupId, memberId);
        return Result.success("已设置管理员", null);
    }

    /**
     * 取消管理员（仅群主）
     */
    @PutMapping("/{groupId}/member/{memberId}/remove-admin")
    public Result<Void> removeAdmin(HttpServletRequest request,
                                    @PathVariable Long groupId,
                                    @PathVariable Long memberId) {
        Long userId = (Long) request.getAttribute("userId");
        groupService.removeAdmin(userId, groupId, memberId);
        return Result.success("已取消管理员", null);
    }

    /**
     * 移除群成员（群主/管理员）
     */
    @DeleteMapping("/{groupId}/member/{memberId}")
    public Result<Void> removeMember(HttpServletRequest request,
                                     @PathVariable Long groupId,
                                     @PathVariable Long memberId) {
        Long userId = (Long) request.getAttribute("userId");
        groupService.removeMember(userId, groupId, memberId);
        return Result.success("已移除成员", null);
    }

    /**
     * 禁言成员（群主/管理员）
     */
    @PutMapping("/{groupId}/member/{memberId}/mute")
    public Result<Void> muteMember(HttpServletRequest request,
                                   @PathVariable Long groupId,
                                   @PathVariable Long memberId,
                                   @RequestBody Map<String, Object> body) {
        Long userId = (Long) request.getAttribute("userId");
        Integer minutes = body.get("minutes") instanceof Integer ? (Integer) body.get("minutes") : 60;
        groupService.muteMember(userId, groupId, memberId, minutes);
        return Result.success("已禁言", null);
    }

    /**
     * 取消禁言（群主/管理员）
     */
    @PutMapping("/{groupId}/member/{memberId}/unmute")
    public Result<Void> unmuteMember(HttpServletRequest request,
                                     @PathVariable Long groupId,
                                     @PathVariable Long memberId) {
        Long userId = (Long) request.getAttribute("userId");
        groupService.unmuteMember(userId, groupId, memberId);
        return Result.success("已取消禁言", null);
    }

    /**
     * 批量禁言（群主/管理员）
     */
    @PutMapping("/{groupId}/members/batch-mute")
    public Result<Void> batchMute(HttpServletRequest request,
                                  @PathVariable Long groupId,
                                  @RequestBody Map<String, Object> body) {
        Long userId = (Long) request.getAttribute("userId");
        @SuppressWarnings("unchecked")
        List<Integer> memberIds = (List<Integer>) body.get("memberIds");
        Integer minutes = body.get("minutes") instanceof Integer ? (Integer) body.get("minutes") : 60;
        groupService.batchMute(userId, groupId, memberIds.stream().map(Long::valueOf).collect(Collectors.toList()), minutes);
        return Result.success("已批量禁言", null);
    }
}