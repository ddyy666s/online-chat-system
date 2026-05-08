package com.chat.chat_backend.service.impl;

import com.chat.chat_backend.module.dto.response.EmojiVO;
import java.util.List;

public interface EmojiService {
    List<EmojiVO> getSystemEmojis();
    List<EmojiVO> getUserEmojis(Long userId);
    EmojiVO addUserEmoji(Long userId, String name, String url, String category);
    void deleteUserEmoji(Long userId, Long emojiId);
}