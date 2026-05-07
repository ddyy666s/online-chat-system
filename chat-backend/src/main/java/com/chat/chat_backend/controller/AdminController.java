package com.chat.chat_backend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chat.chat_backend.common.result.Result;
import com.chat.chat_backend.module.dto.response.MessageAuditVO;
import com.chat.chat_backend.module.dto.response.StatisticsVO;
import com.chat.chat_backend.module.dto.response.UserManageVO;
import com.chat.chat_backend.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    /**
     * 获取用户列表
     */
    @GetMapping("/users")
    public Result<Page<UserManageVO>> getUserList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword) {
        Page<UserManageVO> result = adminService.getUserList(page, size, keyword);
        return Result.success(result);
    }

    /**
     * 禁用/启用用户
     */
    @PutMapping("/user/{userId}/status")
    public Result<Void> updateUserStatus(@PathVariable Long userId, @RequestParam Integer status) {
        adminService.updateUserStatus(userId, status);
        return Result.success("操作成功", null);
    }

    /**
     * 获取聊天记录（审计）
     */
    @GetMapping("/messages")
    public Result<Page<MessageAuditVO>> getMessageList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) Long fromUserId,
            @RequestParam(required = false) Long toUserId,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        Page<MessageAuditVO> result = adminService.getMessageList(page, size, fromUserId, toUserId, startTime, endTime);
        return Result.success(result);
    }

    /**
     * 获取统计数据
     */
    @GetMapping("/stats")
    public Result<StatisticsVO> getStatistics() {
        StatisticsVO result = adminService.getStatistics();
        return Result.success(result);
    }
}