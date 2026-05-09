package com.chat.chat_backend.controller;

import com.chat.chat_backend.common.result.Result;
import com.chat.chat_backend.module.dto.request.HandleFriendRequest;
import com.chat.chat_backend.module.dto.request.MoveFriendGroupRequest;
import com.chat.chat_backend.module.dto.request.SendFriendRequest;
import com.chat.chat_backend.module.dto.response.FriendGroupVO;
import com.chat.chat_backend.module.dto.response.FriendRequestVO;
import com.chat.chat_backend.module.dto.response.FriendVO;
import com.chat.chat_backend.service.friend.FriendRelationService;
import com.chat.chat_backend.service.friend.FriendRequestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/friend")
@RequiredArgsConstructor
public class FriendController {

    private final FriendRelationService friendRelationService;
    private final FriendRequestService friendRequestService;

    @GetMapping("/search")
    public Result<List<FriendVO>> searchUsers(HttpServletRequest request, @RequestParam String keyword) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(friendRelationService.searchUsers(userId, keyword));
    }

    @PostMapping("/request")
    public Result<Void> sendFriendRequest(HttpServletRequest request, @RequestBody SendFriendRequest req) {
        Long userId = (Long) request.getAttribute("userId");
        friendRequestService.sendFriendRequest(userId, req);
        return Result.success("好友申请已发送", null);
    }

    @GetMapping("/requests")
    public Result<List<FriendRequestVO>> getFriendRequests(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(friendRequestService.getFriendRequests(userId));
    }

    @PutMapping("/request/{requestId}")
    public Result<Void> handleFriendRequest(HttpServletRequest request,
                                            @PathVariable Long requestId,
                                            @RequestBody HandleFriendRequest req) {
        Long userId = (Long) request.getAttribute("userId");
        friendRequestService.handleFriendRequest(userId, requestId, req);
        String msg = req.getStatus() == 1 ? "已同意" : "已拒绝";
        return Result.success(msg, null);
    }

    @GetMapping("/list")
    public Result<List<FriendGroupVO>> getFriendList(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(friendRelationService.getFriendList(userId));
    }

    @DeleteMapping("/{friendId}")
    public Result<Void> deleteFriend(HttpServletRequest request, @PathVariable Long friendId) {
        Long userId = (Long) request.getAttribute("userId");
        friendRelationService.deleteFriend(userId, friendId);
        return Result.success("删除成功", null);
    }

    @PutMapping("/{friendId}/group")
    public Result<Void> moveFriendGroup(HttpServletRequest request,
                                        @PathVariable Long friendId,
                                        @RequestBody MoveFriendGroupRequest req) {
        Long userId = (Long) request.getAttribute("userId");
        friendRelationService.moveFriendGroup(userId, friendId, req);
        return Result.success("移动成功", null);
    }

    @PutMapping("/{friendId}/remark")
    public Result<Void> updateFriendRemark(HttpServletRequest request,
                                           @PathVariable Long friendId,
                                           @RequestParam String remark) {
        Long userId = (Long) request.getAttribute("userId");
        friendRelationService.updateFriendRemark(userId, friendId, remark);
        return Result.success("修改成功", null);
    }
}