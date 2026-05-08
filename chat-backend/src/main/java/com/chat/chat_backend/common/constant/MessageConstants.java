package com.chat.chat_backend.common.constant;

public class MessageConstants {

    // ========== 消息类型 ==========
    public static final int MSG_TYPE_TEXT = 1;
    public static final int MSG_TYPE_IMAGE = 2;
    public static final int MSG_TYPE_FILE = 3;

    // ========== 消息状态 ==========
    public static final int MSG_UNREAD = 0;
    public static final int MSG_READ = 1;

    // ========== 消息限制 ==========
    public static final int MAX_CONTENT_LENGTH = 500;
    public static final int PAGE_SIZE = 20;
    public static final int RECALL_TIME_LIMIT_MINUTES = 2;

    // ========== 聊天记录下载 ==========
    public static final int MAX_DOWNLOAD_SIZE = 500;   // 最大下载条数
    public static final int DEFAULT_DOWNLOAD_SIZE = 100;  // 默认下载条数

    public static final String DOWNLOAD_FILE_PREFIX = "chat_";
    public static final String DOWNLOAD_FILE_EXTENSION = ".txt";

    // ========== 消息类型 ==========
     // 文件
    public static final int MSG_TYPE_VOICE = 4;      // 语音
    public static final int MSG_TYPE_VIDEO_CALL = 5; // 视频通话
    public static final int MSG_TYPE_VOICE_CALL = 6; // 语音通话
}