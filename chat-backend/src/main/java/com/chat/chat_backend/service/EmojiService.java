package com.chat.chat_backend.service;

import com.chat.chat_backend.module.dto.response.EmojiVO;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface EmojiService {
    List<EmojiVO> getSystemEmojis();
    List<EmojiVO> getUserEmojis(Long userId);
    EmojiVO uploadEmoji(Long userId, String name, MultipartFile file, String category);
    void deleteEmoji(Long userId, Long emojiId);
}