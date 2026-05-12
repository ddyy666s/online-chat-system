package com.chat.chat_backend.modules.group.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chat.chat_backend.common.result.Result;
import com.chat.chat_backend.modules.group.mapper.GroupMessageMapper;
import com.chat.chat_backend.modules.user.mapper.UserMapper;
import com.chat.chat_backend.modules.group.dto.response.GroupMessageVO;
import com.chat.chat_backend.modules.group.entity.GroupMessage;
import com.chat.chat_backend.modules.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/group")
@RequiredArgsConstructor
public class GroupMessageController {

    private final GroupMessageMapper groupMessageMapper;
    private final UserMapper userMapper;

    @GetMapping("/message/{groupId}")
    public Result<Page<GroupMessageVO>> getGroupHistory(
            HttpServletRequest request,
            @PathVariable Long groupId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        
        Long userId = (Long) request.getAttribute("userId");
        int offset = (page - 1) * size;
        
        List<GroupMessage> messages = groupMessageMapper.findHistory(groupId, offset, size);
        
        List<GroupMessageVO> voList = messages.stream()
                .map(msg -> {
                    User fromUser = userMapper.selectById(msg.getFromUserId());
                    return GroupMessageVO.builder()
                            .id(msg.getId())
                            .groupId(msg.getGroupId())
                            .fromUserId(msg.getFromUserId())
                            .fromUserNickname(fromUser != null ? fromUser.getNickname() : "未知用户")
                            .fromUserAvatar(fromUser != null ? fromUser.getAvatar() : null)
                            .content(msg.getRecallTime() != null ? "对方撤回了一条消息" : msg.getContent())
                            .messageType(msg.getMessageType())
                            .sendTime(msg.getSendTime())
                            .build();
                })
                .collect(Collectors.toList());
        
        Page<GroupMessageVO> pageResult = new Page<>(page, size);
        pageResult.setRecords(voList);
        pageResult.setTotal(messages.size());
        
        return Result.success(pageResult);
    }
}