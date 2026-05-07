package com.chat.chat_backend.module.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 消息响应DTO
 */
@Data
@Builder
public class MessageResponse {

    /**
     * 消息ID
     */
    private Long id;

    /**
     * 发送者ID
     */
    private Long fromUserId;

    /**
     * 发送者昵称
     */
    private String fromUserNickname;

    /**
     * 发送者头像
     */
    private String fromUserAvatar;

    /**
     * 接收者ID
     */
    private Long toUserId;

    /**
     * 接收者昵称
     */
    private String toUserNickname;

    /**
     * 消息类型：1-文字，2-图片，3-文件
     */
    private Integer messageType;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 是否已读
     */
    private Boolean isRead;

    /**
     * 是否已撤回
     */
    private Boolean isRecalled;

    /**
     * 发送时间
     */
    private LocalDateTime sendTime;

    /**
     * 阅读时间
     */
    private LocalDateTime readTime;

    /**
     * 撤回时间
     */
    private LocalDateTime recallTime;
}