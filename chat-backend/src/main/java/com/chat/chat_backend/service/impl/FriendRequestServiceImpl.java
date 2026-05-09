package com.chat.chat_backend.service.impl;

import com.chat.chat_backend.common.exception.BusinessException;
import com.chat.chat_backend.common.result.ResultCode;
import com.chat.chat_backend.mapper.FriendMapper;
import com.chat.chat_backend.mapper.FriendRequestMapper;
import com.chat.chat_backend.mapper.UserMapper;
import com.chat.chat_backend.module.dto.request.HandleFriendRequest;
import com.chat.chat_backend.module.dto.request.SendFriendRequest;
import com.chat.chat_backend.module.dto.response.FriendRequestVO;
import com.chat.chat_backend.module.entity.Friend;
import com.chat.chat_backend.module.entity.FriendRequest;
import com.chat.chat_backend.module.entity.User;
import com.chat.chat_backend.service.friend.FriendRequestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FriendRequestServiceImpl implements FriendRequestService {

    private final FriendMapper friendMapper;
    private final FriendRequestMapper friendRequestMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public void sendFriendRequest(Long currentUserId, SendFriendRequest request) {
        if (currentUserId.equals(request.getToUserId())) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "不能添加自己为好友");
        }

        if (userMapper.selectById(request.getToUserId()) == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        if (friendMapper.findFriendRelation(currentUserId, request.getToUserId()) != null) {
            throw new BusinessException(ResultCode.ALREADY_FRIEND);
        }

        if (friendRequestMapper.findPendingRequest(currentUserId, request.getToUserId()) != null) {
            throw new BusinessException(ResultCode.REQUEST_EXISTS);
        }

        FriendRequest friendRequest = new FriendRequest();
        friendRequest.setFromUserId(currentUserId);
        friendRequest.setToUserId(request.getToUserId());
        friendRequest.setMessage(request.getMessage());
        friendRequest.setStatus(0);
        friendRequest.setExpireTime(LocalDateTime.now().plusDays(7));
        friendRequest.setCreatedAt(LocalDateTime.now());
        friendRequestMapper.insert(friendRequest);
    }

    @Override
    public List<FriendRequestVO> getFriendRequests(Long currentUserId) {
        return friendRequestMapper.findPendingRequests(currentUserId).stream()
                .map(req -> {
                    User fromUser = userMapper.selectById(req.getFromUserId());
                    return FriendRequestVO.builder()
                            .id(req.getId())
                            .fromUserId(req.getFromUserId())
                            .fromUserNickname(fromUser != null ? fromUser.getNickname() : "未知用户")
                            .fromUserAvatar(fromUser != null ? fromUser.getAvatar() : null)
                            .message(req.getMessage())
                            .status(req.getStatus())
                            .createdAt(req.getCreatedAt())
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void handleFriendRequest(Long currentUserId, Long requestId, HandleFriendRequest request) {
        FriendRequest friendRequest = friendRequestMapper.selectById(requestId);
        if (friendRequest == null) {
            throw new BusinessException(ResultCode.REQUEST_NOT_FOUND);
        }
        if (!friendRequest.getToUserId().equals(currentUserId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        if (friendRequest.getExpireTime() != null && friendRequest.getExpireTime().isBefore(LocalDateTime.now())) {
            friendRequest.setStatus(3);
            friendRequestMapper.updateById(friendRequest);
            throw new BusinessException(ResultCode.REQUEST_NOT_FOUND.getCode(), "好友申请已过期");
        }

        friendRequest.setStatus(request.getStatus());
        friendRequest.setHandledTime(LocalDateTime.now());
        friendRequestMapper.updateById(friendRequest);

        if (request.getStatus() == 1) {
            Friend friend1 = new Friend();
            friend1.setUserId(currentUserId);
            friend1.setFriendId(friendRequest.getFromUserId());
            friend1.setGroupName("我的好友");
            friend1.setCreatedAt(LocalDateTime.now());
            friendMapper.insert(friend1);

            Friend friend2 = new Friend();
            friend2.setUserId(friendRequest.getFromUserId());
            friend2.setFriendId(currentUserId);
            friend2.setGroupName("我的好友");
            friend2.setCreatedAt(LocalDateTime.now());
            friendMapper.insert(friend2);
        }
    }
}