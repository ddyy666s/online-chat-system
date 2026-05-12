package com.chat.chat_backend.modules.group.dto.request;

import lombok.Data;
import java.util.List;

@Data
public class CreateGroupRequest {
    private String name;
    private String avatar;
    private String notice;
    private List<Long> memberIds;
}