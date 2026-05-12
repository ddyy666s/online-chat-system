package com.chat.chat_backend.modules.message.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chat.chat_backend.modules.group.dto.response.UnreadGroupVO;
import com.chat.chat_backend.modules.message.dto.response.UnreadMessageDetailVO;
import com.chat.chat_backend.modules.message.entity.Message;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MessageMapper extends BaseMapper<Message> {

    List<Message> findChatHistory(@Param("userId") Long userId,
                                  @Param("friendId") Long friendId,
                                  @Param("offset") Integer offset,
                                  @Param("limit") Integer limit);

    Long countChatHistory(@Param("userId") Long userId, @Param("friendId") Long friendId);

    Integer countUnreadTotal(@Param("userId") Long userId);

    List<UnreadGroupVO> groupUnreadByFriend(@Param("userId") Long userId);

    List<UnreadMessageDetailVO> findUnreadMessages(@Param("userId") Long userId);

    Integer markAsRead(@Param("userId") Long userId, @Param("friendId") Long friendId);

    Integer recallMessage(@Param("messageId") Long messageId, @Param("userId") Long userId);
}