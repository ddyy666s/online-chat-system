package com.chat.chat_backend.controller;

import com.chat.chat_backend.common.result.Result;
import com.chat.chat_backend.module.dto.request.AddImpressionRequest;
import com.chat.chat_backend.module.dto.response.ImpressionVO;
import com.chat.chat_backend.service.ImpressionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/impression")
@RequiredArgsConstructor
public class ImpressionController {

    private final ImpressionService impressionService;

    /**
     * 添加评价
     */
    @PostMapping
    public Result<Void> addImpression(HttpServletRequest request, @RequestBody AddImpressionRequest req) {
        Long userId = (Long) request.getAttribute("userId");
        impressionService.addImpression(userId, req);
        return Result.success("评价成功", null);
    }

    /**
     * 获取对我的评价
     */
    @GetMapping("/to-me")
    public Result<List<ImpressionVO>> getImpressionsToMe(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<ImpressionVO> result = impressionService.getImpressionsToMe(userId);
        return Result.success(result);
    }

    /**
     * 获取我给出的评价
     */
    @GetMapping("/by-me")
    public Result<List<ImpressionVO>> getImpressionsByMe(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<ImpressionVO> result = impressionService.getImpressionsByMe(userId);
        return Result.success(result);
    }

    /**
     * 删除评价
     */
    @DeleteMapping("/{impressionId}")
    public Result<Void> deleteImpression(HttpServletRequest request, @PathVariable Long impressionId) {
        Long userId = (Long) request.getAttribute("userId");
        impressionService.deleteImpression(userId, impressionId);
        return Result.success("删除成功", null);
    }
}