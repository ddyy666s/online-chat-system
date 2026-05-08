package com.chat.chat_backend.controller;

import com.chat.chat_backend.common.result.Result;
import com.chat.chat_backend.mapper.GroupMemberMapper;
import com.chat.chat_backend.mapper.UserMapper;
import com.chat.chat_backend.module.dto.request.CreateGroupRequest;
import com.chat.chat_backend.module.dto.request.InviteMemberRequest;
import com.chat.chat_backend.module.dto.response.GroupMemberVO;
import com.chat.chat_backend.module.dto.response.GroupVO;
import com.chat.chat_backend.module.entity.GroupMember;
import com.chat.chat_backend.module.entity.User;
import com.chat.chat_backend.service.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
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
                    .build();
        }).collect(Collectors.toList());
        return Result.success(result);
    }
}