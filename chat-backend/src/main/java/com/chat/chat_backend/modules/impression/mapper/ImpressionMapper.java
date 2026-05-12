package com.chat.chat_backend.modules.impression.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chat.chat_backend.modules.impression.entity.Impression;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface ImpressionMapper extends BaseMapper<Impression> {
    List<Impression> findImpressionsToUser(@Param("userId") Long userId);
    List<Impression> findImpressionsByUser(@Param("userId") Long userId);
    List<Impression> findImpressionBetween(@Param("fromUserId") Long fromUserId,
                                           @Param("toUserId") Long toUserId);
}