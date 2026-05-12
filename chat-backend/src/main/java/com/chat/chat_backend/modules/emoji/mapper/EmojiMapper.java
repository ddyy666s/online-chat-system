package com.chat.chat_backend.modules.emoji.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chat.chat_backend.modules.emoji.entity.Emoji;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface EmojiMapper extends BaseMapper<Emoji> {
    List<Emoji> findSystemEmojis();
    List<Emoji> findUserEmojis(@Param("userId") Long userId);
}