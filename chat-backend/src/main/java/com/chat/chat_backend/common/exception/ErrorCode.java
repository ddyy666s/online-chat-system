package com.chat.chat_backend.common.exception;

/**
 * 系统错误码定义
 * 与 ResultCode 配合使用
 */
public class ErrorCode {

    // ========== 系统级错误 500-599 ==========
    public static final int SYSTEM_ERROR = 500;
    public static final int DB_ERROR = 501;
    public static final int NETWORK_ERROR = 502;

    // ========== 参数错误 400-499 ==========
    public static final int PARAM_ERROR = 400;
    public static final int PARAM_MISSING = 401;
    public static final int PARAM_TYPE_ERROR = 402;

    // ========== 业务错误码（与 ResultCode 对应）==========
    // 用户模块 1001-1099
    public static final int USER_NOT_FOUND = 1001;
    public static final int USERNAME_EXISTS = 1002;
    public static final int PASSWORD_ERROR = 1003;
    public static final int USER_DISABLED = 1004;
    public static final int UNAUTHORIZED = 1005;
    public static final int FORBIDDEN = 1006;

    // 好友模块 2001-2099
    public static final int FRIEND_NOT_FOUND = 2001;
    public static final int ALREADY_FRIEND = 2002;
    public static final int REQUEST_EXISTS = 2003;
    public static final int REQUEST_NOT_FOUND = 2004;

    // 消息模块 3001-3099
    public static final int MESSAGE_NOT_FOUND = 3001;
    public static final int MESSAGE_RECALL_TIMEOUT = 3002;

    // 印象模块 4001-4099
    public static final int IMPRESSION_NOT_FOUND = 4001;
}