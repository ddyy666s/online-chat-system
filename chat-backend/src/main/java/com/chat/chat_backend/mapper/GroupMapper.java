package com.chat.chat_backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chat.chat_backend.module.entity.Group;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.data.repository.query.Param;

import java.util.List;

@Mapper
public interface GroupMapper extends BaseMapper<Group> {
    
    @Select("SELECT g.* FROM chat_group g " +
            "INNER JOIN group_member gm ON g.id = gm.group_id " +
            "WHERE gm.user_id = #{userId}")
    List<Group> findGroupsByUserId(Long userId);

    @Update("UPDATE group_member SET unread_count = 0, last_read_time = NOW() WHERE group_id = #{groupId} AND user_id = #{userId}")
    void clearUnreadCount(@Param("groupId") Long groupId, @Param("userId") Long userId);
}