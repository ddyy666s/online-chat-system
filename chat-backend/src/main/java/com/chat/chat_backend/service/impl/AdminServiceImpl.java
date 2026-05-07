package com.chat.chat_backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chat.chat_backend.common.constant.RedisConstants;
import com.chat.chat_backend.common.exception.BusinessException;
import com.chat.chat_backend.common.result.ResultCode;
import com.chat.chat_backend.common.utils.RedisUtil;
import com.chat.chat_backend.mapper.MessageMapper;
import com.chat.chat_backend.mapper.UserMapper;
import com.chat.chat_backend.module.dto.response.MessageAuditVO;
import com.chat.chat_backend.module.dto.response.StatisticsVO;
import com.chat.chat_backend.module.dto.response.UserManageVO;
import com.chat.chat_backend.module.entity.Message;
import com.chat.chat_backend.module.entity.User;
import com.chat.chat_backend.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserMapper userMapper;
    private final MessageMapper messageMapper;
    private final RedisUtil redisUtil;

    @Override
    public Page<UserManageVO> getUserList(Integer page, Integer size, String keyword) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(User::getUsername, keyword)
                    .or()
                    .like(User::getNickname, keyword);
        }
        wrapper.orderByDesc(User::getCreatedAt);

        Page<User> userPage = userMapper.selectPage(new Page<>(page, size), wrapper);

        List<UserManageVO> records = userPage.getRecords().stream()
                .map(user -> UserManageVO.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .nickname(user.getNickname())
                        .role(user.getRole())
                        .status(user.getStatus())
                        .createdAt(user.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        Page<UserManageVO> result = new Page<>(page, size);
        result.setRecords(records);
        result.setTotal(userPage.getTotal());
        return result;
    }

    @Override
    public void updateUserStatus(Long userId, Integer status) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        // 不能禁用管理员自己
        if ("admin".equals(user.getRole()) && status == 0) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "不能禁用管理员账号");
        }

        user.setStatus(status);
        userMapper.updateById(user);
    }

    @Override
    public Page<MessageAuditVO> getMessageList(Integer page, Integer size,
                                               Long fromUserId, Long toUserId,
                                               String startTime, String endTime) {
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        if (fromUserId != null) {
            wrapper.eq(Message::getFromUserId, fromUserId);
        }
        if (toUserId != null) {
            wrapper.eq(Message::getToUserId, toUserId);
        }
        if (startTime != null && !startTime.isEmpty()) {
            wrapper.ge(Message::getSendTime, LocalDateTime.parse(startTime + "T00:00:00"));
        }
        if (endTime != null && !endTime.isEmpty()) {
            wrapper.le(Message::getSendTime, LocalDateTime.parse(endTime + "T23:59:59"));
        }
        wrapper.orderByDesc(Message::getSendTime);

        Page<Message> msgPage = messageMapper.selectPage(new Page<>(page, size), wrapper);

        // 获取用户信息（简化版，实际可用缓存）
        List<MessageAuditVO> records = msgPage.getRecords().stream()
                .map(msg -> {
                    User fromUser = userMapper.selectById(msg.getFromUserId());
                    User toUser = userMapper.selectById(msg.getToUserId());
                    return MessageAuditVO.builder()
                            .id(msg.getId())
                            .fromUserId(msg.getFromUserId())
                            .fromUserNickname(fromUser != null ? fromUser.getNickname() : "未知")
                            .toUserId(msg.getToUserId())
                            .toUserNickname(toUser != null ? toUser.getNickname() : "未知")
                            .content(msg.getRecallTime() != null ? "[已撤回]" : msg.getContent())
                            .sendTime(msg.getSendTime())
                            .build();
                })
                .collect(Collectors.toList());

        Page<MessageAuditVO> result = new Page<>(page, size);
        result.setRecords(records);
        result.setTotal(msgPage.getTotal());
        return result;
    }

    @Override
    public StatisticsVO getStatistics() {
        // 总用户数
        Long totalUsers = userMapper.selectCount(null);

        // 今日活跃用户（最后登录时间在今天）
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LambdaQueryWrapper<User> activeWrapper = new LambdaQueryWrapper<>();
        activeWrapper.ge(User::getLastLoginTime, todayStart);
        Long todayActiveUsers = userMapper.selectCount(activeWrapper);

        // 今日消息总数
        LambdaQueryWrapper<Message> msgWrapper = new LambdaQueryWrapper<>();
        msgWrapper.ge(Message::getSendTime, todayStart);
        Long todayMessages = messageMapper.selectCount(msgWrapper);

        // 在线用户数
        Integer onlineUsers = redisUtil.getSetMembers(RedisConstants.ONLINE_USERS).size();

        return StatisticsVO.builder()
                .totalUsers(totalUsers)
                .todayActiveUsers(todayActiveUsers)
                .todayMessages(todayMessages)
                .onlineUsers(onlineUsers)
                .build();
    }
}