package com.chat.chat_backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chat.chat_backend.module.entity.Emoji;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface EmojiMapper extends BaseMapper<Emoji> {

    @Select("SELECT * FROM emoji WHERE is_system = 1 ORDER BY category, id")
    List<Emoji> findSystemEmojis();

    @Select("SELECT * FROM emoji WHERE user_id = #{userId} AND is_system = 0 ORDER BY created_at DESC")
    List<Emoji> findUserEmojis(Long userId);
}