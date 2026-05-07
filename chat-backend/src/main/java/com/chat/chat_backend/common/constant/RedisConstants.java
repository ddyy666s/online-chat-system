package com.chat.chat_backend.common.constant;

public class RedisConstants {
    // 在线用户集合
    public static final String ONLINE_USERS = "online:users";

    // 未读消息计数
    public static final String UNREAD_COUNT = "unread:";

    // Token黑名单
    public static final String TOKEN_BLACKLIST = "blacklist:token:";

    // 过期时间（秒）
    public static final long ONLINE_EXPIRE_SECONDS = 1800;  // 30分钟
    public static final long TOKEN_BLACKLIST_EXPIRE_SECONDS = 86400; // 24小时
}