package com.chat.chat_backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chat.chat_backend.module.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    // 自定义查询：根据用户名查询用户
    @Select("SELECT * FROM user WHERE username = #{username}")
    User findByUsername(@Param("username") String username);

    // 自定义查询：统计在线用户数（根据最后登录时间）
    @Select("SELECT COUNT(*) FROM user WHERE last_login_time > DATE_SUB(NOW(), INTERVAL 5 MINUTE)")
    Integer countOnlineUsers();
}