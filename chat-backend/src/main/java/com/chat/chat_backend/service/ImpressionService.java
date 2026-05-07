package com.chat.chat_backend.service;

import com.chat.chat_backend.module.dto.request.AddImpressionRequest;
import com.chat.chat_backend.module.dto.response.ImpressionVO;
import java.util.List;

public interface ImpressionService {

    // 添加评价
    void addImpression(Long userId, AddImpressionRequest request);

    // 获取对我的评价
    List<ImpressionVO> getImpressionsToMe(Long userId);

    // 获取我给出的评价
    List<ImpressionVO> getImpressionsByMe(Long userId);

    // 删除评价（软删除）
    void deleteImpression(Long userId, Long impressionId);
}