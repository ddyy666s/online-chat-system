package com.chat.chat_backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chat.chat_backend.common.constant.RedisConstants;
import com.chat.chat_backend.common.exception.BusinessException;
import com.chat.chat_backend.common.result.ResultCode;
import com.chat.chat_backend.common.utils.RedisUtil;
import com.chat.chat_backend.mapper.FriendMapper;
import com.chat.chat_backend.mapper.FriendRequestMapper;
import com.chat.chat_backend.mapper.UserMapper;
import com.chat.chat_backend.module.dto.request.HandleFriendRequest;
import com.chat.chat_backend.module.dto.request.MoveFriendGroupRequest;
import com.chat.chat_backend.module.dto.request.SendFriendRequest;
import com.chat.chat_backend.module.dto.response.FriendGroupVO;
import com.chat.chat_backend.module.dto.response.FriendRequestVO;
import com.chat.chat_backend.module.dto.response.FriendVO;
import com.chat.chat_backend.module.entity.Friend;
import com.chat.chat_backend.module.entity.FriendRequest;
import com.chat.chat_backend.module.entity.User;
import com.chat.chat_backend.service.FriendService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FriendServiceImpl implements FriendService {

    private final FriendMapper friendMapper;
    private final FriendRequestMapper friendRequestMapper;
    private final UserMapper userMapper;
    private final RedisUtil redisUtil;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public List<FriendVO> searchUsers(Long currentUserId, String keyword) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(User::getUsername, keyword)
                .or()
                .like(User::getNickname, keyword)
                .ne(User::getId, currentUserId)
                .eq(User::getStatus, 1);

        List<User> users = userMapper.selectList(wrapper);

        return users.stream().map(user -> {
            Friend existingFriend = friendMapper.findFriendRelation(currentUserId, user.getId());
            return FriendVO.builder()
                    .userId(user.getId())
                    .nickname(user.getNickname())
                    .avatar(user.getAvatar())
                    .signature(user.getSignature())
                    .remark(existingFriend != null ? existingFriend.getRemark() : null)
                    .groupName(existingFriend != null ? existingFriend.getGroupName() : null)
                    .isOnline(redisUtil.isMember(RedisConstants.ONLINE_USERS, String.valueOf(user.getId())))
                    .unreadCount(0)
                    .build();
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void sendFriendRequest(Long currentUserId, SendFriendRequest request) {
        if (currentUserId.equals(request.getToUserId())) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "不能添加自己为好友");
        }

        User targetUser = userMapper.selectById(request.getToUserId());
        if (targetUser == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        Friend existingFriend = friendMapper.findFriendRelation(currentUserId, request.getToUserId());
        if (existingFriend != null) {
            throw new BusinessException(ResultCode.ALREADY_FRIEND);
        }

        FriendRequest pendingRequest = friendRequestMapper.findPendingRequest(currentUserId, request.getToUserId());
        if (pendingRequest != null) {
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
        List<FriendRequest> requests = friendRequestMapper.findPendingRequests(currentUserId);

        return requests.stream().map(req -> {
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
        }).collect(Collectors.toList());
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

    @Override
    public List<FriendGroupVO> getFriendList(Long currentUserId) {
        // 参数校验
        if (currentUserId == null) {
            log.warn("getFriendList: currentUserId is null");
            return new ArrayList<>();
        }

        List<Friend> friends = friendMapper.findAllByUserId(currentUserId);
        log.info("getFriendList: userId={}, friends count={}", currentUserId, friends.size());

        // 如果没有好友，直接返回空列表
        if (friends == null || friends.isEmpty()) {
            return new ArrayList<>();
        }

        List<Long> friendIds = friends.stream()
                .map(Friend::getFriendId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // 如果所有 friendId 都是 null，返回空列表
        if (friendIds.isEmpty()) {
            return new ArrayList<>();
        }

        List<User> users = userMapper.selectBatchIds(friendIds);
        Map<Long, User> userMap = users.stream().collect(Collectors.toMap(User::getId, u -> u, (u1, u2) -> u1));

        String unreadKey = RedisConstants.UNREAD_COUNT + currentUserId;
        Map<Object, Object> unreadMap = redisTemplate.opsForHash().entries(unreadKey);

        Map<String, List<FriendVO>> groupMap = new HashMap<>();

        for (Friend friend : friends) {
            User friendUser = userMap.get(friend.getFriendId());
            if (friendUser == null) continue;

            String groupName = friend.getGroupName() != null ? friend.getGroupName() : "我的好友";
            Integer unreadCount = 0;
            if (unreadMap.containsKey(String.valueOf(friend.getFriendId()))) {
                Object value = unreadMap.get(String.valueOf(friend.getFriendId()));
                if (value != null) {
                    unreadCount = Integer.parseInt(value.toString());
                }
            }

            FriendVO vo = FriendVO.builder()
                    .id(friend.getId())
                    .userId(friend.getFriendId())
                    .nickname(friendUser.getNickname())
                    .avatar(friendUser.getAvatar())
                    .signature(friendUser.getSignature())
                    .remark(friend.getRemark())
                    .groupName(groupName)
                    .isOnline(redisUtil.isMember(RedisConstants.ONLINE_USERS, String.valueOf(friend.getFriendId())))
                    .unreadCount(unreadCount)
                    .build();

            groupMap.computeIfAbsent(groupName, k -> new ArrayList<>()).add(vo);
        }

        return groupMap.entrySet().stream()
                .map(entry -> FriendGroupVO.builder()
                        .groupName(entry.getKey())
                        .friends(entry.getValue())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteFriend(Long currentUserId, Long friendId) {
        LambdaQueryWrapper<Friend> wrapper1 = new LambdaQueryWrapper<>();
        wrapper1.eq(Friend::getUserId, currentUserId).eq(Friend::getFriendId, friendId);
        friendMapper.delete(wrapper1);

        LambdaQueryWrapper<Friend> wrapper2 = new LambdaQueryWrapper<>();
        wrapper2.eq(Friend::getUserId, friendId).eq(Friend::getFriendId, currentUserId);
        friendMapper.delete(wrapper2);
    }

    @Override
    public void moveFriendGroup(Long currentUserId, Long friendId, MoveFriendGroupRequest request) {
        LambdaQueryWrapper<Friend> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Friend::getUserId, currentUserId).eq(Friend::getFriendId, friendId);

        Friend friend = friendMapper.selectOne(wrapper);
        if (friend == null) {
            throw new BusinessException(ResultCode.FRIEND_NOT_FOUND);
        }

        friend.setGroupName(request.getGroupName());
        friendMapper.updateById(friend);
    }

    @Override
    public void updateFriendRemark(Long currentUserId, Long friendId, String remark) {
        LambdaQueryWrapper<Friend> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Friend::getUserId, currentUserId).eq(Friend::getFriendId, friendId);

        Friend friend = friendMapper.selectOne(wrapper);
        if (friend == null) {
            throw new BusinessException(ResultCode.FRIEND_NOT_FOUND);
        }

        friend.setRemark(remark);
        friendMapper.updateById(friend);
    }
}