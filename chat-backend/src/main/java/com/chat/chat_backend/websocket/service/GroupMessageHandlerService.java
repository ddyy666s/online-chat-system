package com.chat.chat_backend.websocket.service;

import cn.hutool.json.JSONUtil;
import com.chat.chat_backend.mapper.GroupMapper;
import com.chat.chat_backend.mapper.GroupMemberMapper;
import com.chat.chat_backend.mapper.GroupMessageMapper;
import com.chat.chat_backend.mapper.UserMapper;
import com.chat.chat_backend.module.entity.GroupMember;
import com.chat.chat_backend.module.entity.GroupMessage;
import com.chat.chat_backend.module.entity.User;
import com.chat.chat_backend.websocket.WebSocketSessionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GroupMessageHandlerService {

    private final GroupMapper groupMapper;
    private final GroupMemberMapper groupMemberMapper;
    private final GroupMessageMapper groupMessageMapper;
    private final UserMapper userMapper;
    private final WebSocketSessionManager sessionManager;

    public void sendAndNotify(Long fromUserId, Long groupId, String content, Integer messageType, Integer duration) {
        // 验证群成员
        List<GroupMember> members = groupMemberMapper.findByGroupId(groupId);
        boolean isMember = members.stream().anyMatch(m -> m.getUserId().equals(fromUserId));
        if (!isMember) {
            log.error("用户 {} 不是群 {} 的成员", fromUserId, groupId);
            return;
        }

        // 保存群消息
        GroupMessage msg = new GroupMessage();
        msg.setGroupId(groupId);
        msg.setFromUserId(fromUserId);
        msg.setMessageType(messageType);
        msg.setContent(content);
        msg.setSendTime(LocalDateTime.now());
        groupMessageMapper.insert(msg);

        // 增加未读计数
        groupMemberMapper.incrementUnreadCount(groupId, fromUserId);

        // 获取发送者信息
        User fromUser = userMapper.selectById(fromUserId);
        String fromUserNickname = fromUser != null ? fromUser.getNickname() : "未知用户";

        // 构建响应
        var response = JSONUtil.createObj()
                .set("type", "group_message")
                .set("messageId", msg.getId())
                .set("groupId", groupId)
                .set("fromUserId", fromUserId)
                .set("fromUserNickname", fromUserNickname)
                .set("content", content)
                .set("messageType", messageType)
                .set("duration", duration)
                .set("sendTime", msg.getSendTime().toString());

        String responseStr = response.toString();

        // 推送给群内其他在线成员
        for (GroupMember member : members) {
            if (!member.getUserId().equals(fromUserId) && sessionManager.isOnline(member.getUserId())) {
                sessionManager.sendMessage(member.getUserId(), responseStr);
            }
        }
    }
}