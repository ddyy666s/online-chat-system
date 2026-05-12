package com.chat.chat_backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chat.chat_backend.module.entity.NotificationRead;
import com.chat.chat_backend.module.entity.SystemNotification;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface SystemNotificationMapper extends BaseMapper<SystemNotification> {

    @Select("SELECT sn.* FROM system_notification sn " +
            "WHERE sn.id NOT IN (SELECT notification_id FROM notification_read WHERE user_id = #{userId}) " +
            "ORDER BY sn.created_at DESC")
    List<SystemNotification> findUnreadByUserId(@Param("userId") Long userId);

    @Select("SELECT * FROM system_notification WHERE admin_id = #{adminId} ORDER BY created_at DESC")
    List<SystemNotification> findByAdminId(@Param("adminId") Long adminId);

    @Select("SELECT COUNT(*) FROM system_notification sn " +
            "WHERE sn.id NOT IN (SELECT notification_id FROM notification_read WHERE user_id = #{userId})")
    Integer countUnreadByUserId(@Param("userId") Long userId);

    @Insert("INSERT INTO notification_read (notification_id, user_id, read_at) VALUES (#{notificationId}, #{userId}, NOW())")
    int markAsRead(@Param("notificationId") Long notificationId, @Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM notification_read WHERE notification_id = #{notificationId} AND user_id = #{userId}")
    int existsRead(@Param("notificationId") Long notificationId, @Param("userId") Long userId);
}
