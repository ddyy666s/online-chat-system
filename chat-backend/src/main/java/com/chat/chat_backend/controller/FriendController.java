package com.chat.chat_backend.controller;

import com.chat.chat_backend.common.result.Result;
import com.chat.chat_backend.module.dto.request.HandleFriendRequest;
import com.chat.chat_backend.module.dto.request.MoveFriendGroupRequest;
import com.chat.chat_backend.module.dto.request.SendFriendRequest;
import com.chat.chat_backend.module.dto.response.FriendGroupVO;
import com.chat.chat_backend.module.dto.response.FriendRequestVO;
import com.chat.chat_backend.module.dto.response.FriendVO;
import com.chat.chat_backend.service.FriendService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/friend")
@RequiredArgsConstructor
public class FriendController {

    private final FriendService friendService;

    /**
     * 搜索用户
     */
    @GetMapping("/search")
    public Result<List<FriendVO>> searchUsers(HttpServletRequest request, @RequestParam String keyword) {
        Long userId = (Long) request.getAttribute("userId");
        List<FriendVO> result = friendService.searchUsers(userId, keyword);
        return Result.success(result);
    }

    /**
     * 发送好友申请
     */
    @PostMapping("/request")
    public Result<Void> sendFriendRequest(HttpServletRequest request, @RequestBody SendFriendRequest sendRequest) {
        Long userId = (Long) request.getAttribute("userId");
        friendService.sendFriendRequest(userId, sendRequest);
        return Result.success("好友申请已发送", null);
    }

    /**
     * 获取好友申请列表
     */
    @GetMapping("/requests")
    public Result<List<FriendRequestVO>> getFriendRequests(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<FriendRequestVO> result = friendService.getFriendRequests(userId);
        return Result.success(result);
    }

    /**
     * 处理好友申请
     */
    @PutMapping("/request/{requestId}")
    public Result<Void> handleFriendRequest(HttpServletRequest request,
                                            @PathVariable Long requestId,
                                            @RequestBody HandleFriendRequest handleRequest) {
        Long userId = (Long) request.getAttribute("userId");
        friendService.handleFriendRequest(userId, requestId, handleRequest);
        String message = handleRequest.getStatus() == 1 ? "已同意好友申请" : "已拒绝好友申请";
        return Result.success(message, null);
    }

    /**
     * 获取好友列表
     */
    @GetMapping("/list")
    public Result<List<FriendGroupVO>> getFriendList(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<FriendGroupVO> result = friendService.getFriendList(userId);
        return Result.success(result);
    }

    /**
     * 删除好友
     */
    @DeleteMapping("/{friendId}")
    public Result<Void> deleteFriend(HttpServletRequest request, @PathVariable Long friendId) {
        Long userId = (Long) request.getAttribute("userId");
        friendService.deleteFriend(userId, friendId);
        return Result.success("删除成功", null);
    }

    /**
     * 移动好友分组
     */
    @PutMapping("/{friendId}/group")
    public Result<Void> moveFriendGroup(HttpServletRequest request,
                                        @PathVariable Long friendId,
                                        @RequestBody MoveFriendGroupRequest moveRequest) {
        Long userId = (Long) request.getAttribute("userId");
        friendService.moveFriendGroup(userId, friendId, moveRequest);
        return Result.success("移动成功", null);
    }

    /**
     * 修改好友备注
     */
    @PutMapping("/{friendId}/remark")
    public Result<Void> updateFriendRemark(HttpServletRequest request,
                                           @PathVariable Long friendId,
                                           @RequestParam String remark) {
        Long userId = (Long) request.getAttribute("userId");
        friendService.updateFriendRemark(userId, friendId, remark);
        return Result.success("修改成功", null);
    }
}