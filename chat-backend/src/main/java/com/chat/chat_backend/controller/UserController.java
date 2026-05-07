package com.chat.chat_backend.controller;

import com.chat.chat_backend.common.result.Result;
import com.chat.chat_backend.module.dto.request.LoginRequest;
import com.chat.chat_backend.module.dto.request.RegisterRequest;
import com.chat.chat_backend.module.dto.response.LoginResponse;
import com.chat.chat_backend.module.dto.response.UserInfoResponse;
import com.chat.chat_backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public Result<Void> register(@RequestBody RegisterRequest request) {
        userService.register(request);
        return Result.success("注册成功", null);
    }

    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody LoginRequest request) {
        LoginResponse response = userService.login(request);
        return Result.success(response);
    }

    @GetMapping("/me")
    public Result<UserInfoResponse> getMe(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        UserInfoResponse response = userService.getUserInfo(userId);
        return Result.success(response);
    }
}