package com.chat.chat_backend.common.result;

import lombok.Getter;

@Getter
public enum ResultCode {
    SUCCESS(200, "操作成功"),
    ERROR(500, "操作失败"),

    // 用户模块错误码 1001-1999
    USER_NOT_FOUND(1001, "用户不存在"),
    USERNAME_EXISTS(1002, "用户名已存在"),
    PASSWORD_ERROR(1003, "密码错误"),
    USER_DISABLED(1004, "账号已被禁用"),
    UNAUTHORIZED(1005, "未登录或token已过期"),
    FORBIDDEN(1006, "无权限访问"),

    // 好友模块错误码 2001-2999
    FRIEND_NOT_FOUND(2001, "好友不存在"),
    ALREADY_FRIEND(2002, "已经是好友关系"),
    REQUEST_EXISTS(2003, "好友申请已发送"),
    REQUEST_NOT_FOUND(2004, "申请记录不存在"),

    // 消息模块错误码 3001-3999
    MESSAGE_NOT_FOUND(3001, "消息不存在"),
    MESSAGE_RECALL_TIMEOUT(3002, "消息发送已超过2分钟，无法撤回"),

    // 印象模块错误码 4001-4999
    IMPRESSION_NOT_FOUND(4001, "评价不存在"),

    // 参数错误
    PARAM_ERROR(400, "参数错误");

    private final Integer code;
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}