package com.chat.chat_backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chat.chat_backend.module.entity.Impression;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface ImpressionMapper extends BaseMapper<Impression> {

    @Select("SELECT * FROM impression WHERE to_user_id = #{userId} AND is_delete = 0 ORDER BY created_at DESC")
    List<Impression> findImpressionsToUser(@Param("userId") Long userId);

    @Select("SELECT * FROM impression WHERE from_user_id = #{userId} AND is_delete = 0 ORDER BY created_at DESC")
    List<Impression> findImpressionsByUser(@Param("userId") Long userId);

    @Select("SELECT * FROM impression WHERE from_user_id = #{fromUserId} AND to_user_id = #{toUserId} AND is_delete = 0")
    List<Impression> findImpressionBetween(@Param("fromUserId") Long fromUserId,
                                           @Param("toUserId") Long toUserId);
}