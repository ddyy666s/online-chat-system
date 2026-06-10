package com.chat.chat_backend.modules.group.controller;

import com.chat.chat_backend.common.annotation.LoginUser;
import com.chat.chat_backend.common.result.Result;
import com.chat.chat_backend.modules.group.dto.request.CreateGroupRequest;
import com.chat.chat_backend.modules.group.dto.request.InviteMemberRequest;
import com.chat.chat_backend.modules.group.dto.response.GroupMemberVO;
import com.chat.chat_backend.modules.group.dto.response.GroupVO;
import com.chat.chat_backend.modules.group.service.GroupService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 群聊控制器（优化版）
 */
@Slf4j
@RestController
@RequestMapping("/groups")
@RequiredArgsConstructor
@Tag(name = "群聊管理")
public class GroupController {

    private final GroupService groupService;

    @Operation(summary = "创建群聊")
    @PostMapping
    public Result<GroupVO> createGroup(@LoginUser Long userId,
                                       @Valid @RequestBody CreateGroupRequest req) {

        log.info("用户 [{}] 创建群聊", userId);
        return Result.success(groupService.createGroup(userId, req));
    }

    @Operation(summary = "用户群列表")
    @GetMapping
    public Result<List<GroupVO>> getUserGroups(@LoginUser Long userId) {

        return Result.success(groupService.getUserGroups(userId));
    }

    @Operation(summary = "群详情")
    @GetMapping("/{groupId}")
    public Result<GroupVO> getGroupDetail(@LoginUser Long userId,
                                          @PathVariable Long groupId) {

        return Result.success(groupService.getGroupDetail(userId, groupId));
    }

    @Operation(summary = "邀请成员")
    @PostMapping("/{groupId}/members")
    public Result<Void> inviteMember(@LoginUser Long userId,
                                     @PathVariable Long groupId,
                                     @Valid @RequestBody InviteMemberRequest req) {

        groupService.inviteMember(userId, groupId, req);
        return Result.success();
    }

    @Operation(summary = "退出群聊")
    @DeleteMapping("/{groupId}/members/me")
    public Result<Void> quitGroup(@LoginUser Long userId,
                                  @PathVariable Long groupId) {

        groupService.quitGroup(userId, groupId);
        return Result.success();
    }

    @Operation(summary = "解散群聊")
    @DeleteMapping("/{groupId}")
    public Result<Void> disbandGroup(@LoginUser Long userId,
                                     @PathVariable Long groupId) {

        groupService.disbandGroup(userId, groupId);
        return Result.success();
    }

    @Operation(summary = "群成员列表")
    @GetMapping("/{groupId}/members")
    public Result<List<GroupMemberVO>> getGroupMembers(@LoginUser Long userId,
                                                       @PathVariable Long groupId) {

        return Result.success(groupService.getGroupMembers(userId, groupId));
    }

    @Operation(summary = "清除未读")
    @PutMapping("/{groupId}/read")
    public Result<Void> clearUnreadCount(@LoginUser Long userId,
                                         @PathVariable Long groupId) {

        groupService.clearUnreadCount(userId, groupId);
        return Result.success();
    }
}