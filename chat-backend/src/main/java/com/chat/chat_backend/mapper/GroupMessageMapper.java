package com.chat.chat_backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chat.chat_backend.module.entity.GroupMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface GroupMessageMapper extends BaseMapper<GroupMessage> {
    
    @Select("SELECT * FROM group_message WHERE group_id = #{groupId} " +
            "ORDER BY send_time DESC LIMIT #{limit} OFFSET #{offset}")
    List<GroupMessage> findHistory(@Param("groupId") Long groupId,
                                    @Param("offset") Integer offset,
                                    @Param("limit") Integer limit);
}