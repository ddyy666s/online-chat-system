package com.chat.chat_backend.service.impl;

import com.chat.chat_backend.common.exception.BusinessException;
import com.chat.chat_backend.common.result.ResultCode;
import com.chat.chat_backend.mapper.ImpressionMapper;
import com.chat.chat_backend.mapper.UserMapper;
import com.chat.chat_backend.module.dto.request.AddImpressionRequest;
import com.chat.chat_backend.module.dto.response.ImpressionVO;
import com.chat.chat_backend.module.entity.Impression;
import com.chat.chat_backend.module.entity.User;
import com.chat.chat_backend.service.ImpressionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ImpressionServiceImpl implements ImpressionService {

    private final ImpressionMapper impressionMapper;
    private final UserMapper userMapper;

    @Override
    public void addImpression(Long userId, AddImpressionRequest request) {
        // 不能评价自己
        if (userId.equals(request.getToUserId())) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "不能评价自己");
        }

        // 检查目标用户是否存在
        User targetUser = userMapper.selectById(request.getToUserId());
        if (targetUser == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        // 内容长度限制
        if (request.getContent().length() > 100) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "评价内容不能超过100字");
        }

        Impression impression = new Impression();
        impression.setFromUserId(userId);
        impression.setToUserId(request.getToUserId());
        impression.setContent(request.getContent());
        impression.setIsDelete(0);
        impression.setCreatedAt(LocalDateTime.now());

        impressionMapper.insert(impression);
    }

    @Override
    public List<ImpressionVO> getImpressionsToMe(Long userId) {
        List<Impression> impressions = impressionMapper.findImpressionsToUser(userId);

        return impressions.stream().map(imp -> {
            User fromUser = userMapper.selectById(imp.getFromUserId());
            return ImpressionVO.builder()
                    .id(imp.getId())
                    .fromUserId(imp.getFromUserId())
                    .fromUserNickname(fromUser != null ? fromUser.getNickname() : "未知用户")
                    .fromUserAvatar(fromUser != null ? fromUser.getAvatar() : null)
                    .toUserId(imp.getToUserId())
                    .content(imp.getContent())
                    .createdAt(imp.getCreatedAt())
                    .build();
        }).collect(Collectors.toList());
    }

    @Override
    public List<ImpressionVO> getImpressionsByMe(Long userId) {
        List<Impression> impressions = impressionMapper.findImpressionsByUser(userId);

        return impressions.stream().map(imp -> {
            User toUser = userMapper.selectById(imp.getToUserId());
            return ImpressionVO.builder()
                    .id(imp.getId())
                    .fromUserId(imp.getFromUserId())
                    .toUserId(imp.getToUserId())
                    .toUserNickname(toUser != null ? toUser.getNickname() : "未知用户")
                    .toUserAvatar(toUser != null ? toUser.getAvatar() : null)
                    .content(imp.getContent())
                    .createdAt(imp.getCreatedAt())
                    .build();
        }).collect(Collectors.toList());
    }

    @Override
    public void deleteImpression(Long userId, Long impressionId) {
        Impression impression = impressionMapper.selectById(impressionId);
        if (impression == null) {
            throw new BusinessException(ResultCode.IMPRESSION_NOT_FOUND);
        }

        // 只有评价者可以删除
        if (!impression.getFromUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }

        impression.setIsDelete(1);
        impressionMapper.updateById(impression);
    }
}