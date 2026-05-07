package com.chat.chat_backend.common.constant;

public class MessageConstants {
    // 消息类型
    public static final int MSG_TYPE_TEXT = 1;
    public static final int MSG_TYPE_IMAGE = 2;
    public static final int MSG_TYPE_FILE = 3;

    // 消息最大长度
    public static final int MAX_CONTENT_LENGTH = 500;

    // 可撤回时间（分钟）
    public static final int RECALL_TIME_LIMIT_MINUTES = 2;
}