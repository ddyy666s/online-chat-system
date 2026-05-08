package com.chat.chat_backend.service;

import com.chat.chat_backend.module.dto.request.LoginRequest;
import com.chat.chat_backend.module.dto.request.RegisterRequest;
import com.chat.chat_backend.module.dto.request.UpdateProfileRequest;
import com.chat.chat_backend.module.dto.response.LoginResponse;
import com.chat.chat_backend.module.dto.response.UserInfoResponse;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {
    void register(RegisterRequest request);
    LoginResponse login(LoginRequest request);
    UserInfoResponse getUserInfo(Long userId);
    UserInfoResponse updateProfile(Long userId, UpdateProfileRequest request);
    String updateAvatar(Long userId, MultipartFile file);
}