package com.chat.chat_backend.modules.emoji.controller;

import com.chat.chat_backend.common.result.Result;
import com.chat.chat_backend.modules.emoji.dto.response.EmojiVO;
import com.chat.chat_backend.modules.emoji.service.EmojiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/emoji")
@RequiredArgsConstructor
public class EmojiController {

    private final EmojiService emojiService;

    @GetMapping("/system")
    public Result<List<EmojiVO>> getSystemEmojis() {
        return Result.success(emojiService.getSystemEmojis());
    }

    @GetMapping("/user")
    public Result<List<EmojiVO>> getUserEmojis(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(emojiService.getUserEmojis(userId));
    }

    @PostMapping("/upload")
    public Result<EmojiVO> uploadEmoji(HttpServletRequest request,
                                       @RequestParam("file") MultipartFile file,
                                       @RequestParam("name") String name,
                                       @RequestParam(value = "category", required = false) String category) {
        Long userId = (Long) request.getAttribute("userId");
        EmojiVO result = emojiService.uploadEmoji(userId, name, file, category);
        return Result.success(result);
    }

    @DeleteMapping("/{emojiId}")
    public Result<Void> deleteEmoji(HttpServletRequest request, @PathVariable Long emojiId) {
        Long userId = (Long) request.getAttribute("userId");
        emojiService.deleteEmoji(userId, emojiId);
        return Result.success("删除成功", null);
    }
}