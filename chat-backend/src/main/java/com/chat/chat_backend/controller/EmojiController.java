package com.chat.chat_backend.controller;

import com.chat.chat_backend.common.result.Result;
import com.chat.chat_backend.common.utils.OssUtil;
import com.chat.chat_backend.mapper.EmojiMapper;
import com.chat.chat_backend.module.dto.response.EmojiVO;
import com.chat.chat_backend.module.entity.Emoji;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/emoji")
@RequiredArgsConstructor
public class EmojiController {

    private final EmojiMapper emojiMapper;
    private final OssUtil ossUtil;

    /**
     * 获取系统表情包
     */
    @GetMapping("/system")
    public Result<List<EmojiVO>> getSystemEmojis() {
        List<Emoji> emojis = emojiMapper.findSystemEmojis();
        List<EmojiVO> result = emojis.stream()
                .map(this::toVO)
                .collect(Collectors.toList());
        return Result.success(result);
    }

    /**
     * 获取用户自定义表情包
     */
    @GetMapping("/user")
    public Result<List<EmojiVO>> getUserEmojis(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<Emoji> emojis = emojiMapper.findUserEmojis(userId);
        List<EmojiVO> result = emojis.stream()
                .map(this::toVO)
                .collect(Collectors.toList());
        return Result.success(result);
    }

    /**
     * 上传自定义表情
     */
    @PostMapping("/upload")
    public Result<EmojiVO> uploadEmoji(HttpServletRequest request,
                                        @RequestParam("file") MultipartFile file,
                                        @RequestParam("name") String name,
                                        @RequestParam(value = "category", required = false) String category) {
        Long userId = (Long) request.getAttribute("userId");
        
        try {
            String url = ossUtil.uploadFile(file, "emoji/");
            
            Emoji emoji = new Emoji();
            emoji.setName(name);
            emoji.setUrl(url);
            emoji.setCategory(category != null ? category : "custom");
            emoji.setUserId(userId);
            emoji.setIsSystem(0);
            emoji.setCreatedAt(LocalDateTime.now());
            emojiMapper.insert(emoji);
            
            return Result.success(toVO(emoji));
        } catch (Exception e) {
            log.error("上传表情失败", e);
            return Result.error("上传失败");
        }
    }

    /**
     * 删除自定义表情
     */
    @DeleteMapping("/{emojiId}")
    public Result<Void> deleteEmoji(HttpServletRequest request, @PathVariable Long emojiId) {
        Long userId = (Long) request.getAttribute("userId");
        Emoji emoji = emojiMapper.selectById(emojiId);
        if (emoji == null || !emoji.getUserId().equals(userId)) {
            return Result.error("无权限删除");
        }
        emojiMapper.deleteById(emojiId);
        return Result.success("删除成功", null);
    }

    private EmojiVO toVO(Emoji emoji) {
        return EmojiVO.builder()
                .id(emoji.getId())
                .name(emoji.getName())
                .url(emoji.getUrl())
                .category(emoji.getCategory())
                .userId(emoji.getUserId())
                .isSystem(emoji.getIsSystem() == 1)
                .createdAt(emoji.getCreatedAt())
                .build();
    }
}