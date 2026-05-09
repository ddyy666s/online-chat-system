package com.chat.chat_backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chat.chat_backend.module.entity.GroupMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface GroupMessageMapper extends BaseMapper<GroupMessage> {
    List<GroupMessage> findHistory(@Param("groupId") Long groupId,
                                   @Param("offset") Integer offset,
                                   @Param("limit") Integer limit);
}