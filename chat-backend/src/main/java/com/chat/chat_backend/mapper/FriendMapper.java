package com.chat.chat_backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chat.chat_backend.module.entity.Friend;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface FriendMapper extends BaseMapper<Friend> {

    @Select("SELECT f.* FROM friend f WHERE f.user_id = #{userId} AND f.friend_id = #{friendId}")
    Friend findFriendRelation(@Param("userId") Long userId, @Param("friendId") Long friendId);

    @Select("SELECT f.* FROM friend f WHERE f.user_id = #{userId}")
    List<Friend> findAllByUserId(@Param("userId") Long userId);
}