package com.chat.chat_backend.modules.friend.dto.response;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class FriendGroupVO {
    private String groupName;
    private List<FriendVO> friends;
}