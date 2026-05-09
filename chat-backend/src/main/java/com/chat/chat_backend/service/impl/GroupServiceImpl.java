package com.chat.chat_backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chat.chat_backend.common.exception.BusinessException;
import com.chat.chat_backend.common.result.ResultCode;
import com.chat.chat_backend.mapper.*;
import com.chat.chat_backend.module.dto.request.CreateGroupRequest;
import com.chat.chat_backend.module.dto.request.InviteMemberRequest;
import com.chat.chat_backend.module.dto.response.GroupVO;
import com.chat.chat_backend.module.entity.Group;
import com.chat.chat_backend.module.entity.GroupMember;
import com.chat.chat_backend.module.entity.User;
import com.chat.chat_backend.service.GroupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GroupServiceImpl implements GroupService {

    private final GroupMapper groupMapper;
    private final GroupMemberMapper groupMemberMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public GroupVO createGroup(Long userId, CreateGroupRequest request) {
        // 1. 创建群聊
        Group group = new Group();
        group.setName(request.getName());
        group.setAvatar(request.getAvatar());
        group.setOwnerId(userId);
        group.setNotice(request.getNotice());
        group.setMemberCount(1 + (request.getMemberIds() != null ? request.getMemberIds().size() : 0));
        group.setCreatedAt(LocalDateTime.now());
        groupMapper.insert(group);
        
        // 2. 添加群主为成员
        GroupMember owner = new GroupMember();
        owner.setGroupId(group.getId());
        owner.setUserId(userId);
        owner.setRole(2); // 群主
        owner.setJoinTime(LocalDateTime.now());
        groupMemberMapper.insert(owner);
        
        // 3. 添加其他成员
        if (request.getMemberIds() != null) {
            for (Long memberId : request.getMemberIds()) {
                if (memberId.equals(userId)) continue;
                GroupMember member = new GroupMember();
                member.setGroupId(group.getId());
                member.setUserId(memberId);
                member.setRole(0);
                member.setJoinTime(LocalDateTime.now());
                groupMemberMapper.insert(member);
            }
        }
        
        return getGroupDetail(userId, group.getId());
    }

    @Override
    public List<GroupVO> getUserGroups(Long userId) {
        List<Group> groups = groupMapper.findGroupsByUserId(userId);
        if (groups.isEmpty()) return new ArrayList<>();

        // 一次查询所有群成员信息
        List<Long> groupIds = groups.stream().map(Group::getId).collect(Collectors.toList());
        LambdaQueryWrapper<GroupMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GroupMember::getUserId, userId)
                .in(GroupMember::getGroupId, groupIds);
        List<GroupMember> members = groupMemberMapper.selectList(wrapper);
        Map<Long, Integer> unreadMap = members.stream()
                .collect(Collectors.toMap(GroupMember::getGroupId, GroupMember::getUnreadCount));

        return groups.stream()
                .map(group -> GroupVO.builder()
                        .id(group.getId())
                        .name(group.getName())
                        .avatar(group.getAvatar())
                        .notice(group.getNotice())
                        .ownerId(group.getOwnerId())
                        .memberCount(group.getMemberCount())
                        .unreadCount(unreadMap.getOrDefault(group.getId(), 0))
                        .createdAt(group.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public GroupVO getGroupDetail(Long userId, Long groupId) {
        Group group = groupMapper.selectById(groupId);
        if (group == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "群聊不存在");
        }
        
        // 检查是否是成员
        LambdaQueryWrapper<GroupMember> checkWrapper = new LambdaQueryWrapper<>();
        checkWrapper.eq(GroupMember::getGroupId, groupId)
                    .eq(GroupMember::getUserId, userId);
        if (groupMemberMapper.selectCount(checkWrapper) == 0) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "你不是群成员");
        }
        
        return GroupVO.builder()
                .id(group.getId())
                .name(group.getName())
                .avatar(group.getAvatar())
                .notice(group.getNotice())
                .ownerId(group.getOwnerId())
                .memberCount(group.getMemberCount())
                .createdAt(group.getCreatedAt())
                .build();
    }

    @Override
    @Transactional
    public void inviteMember(Long userId, InviteMemberRequest request) {
        // 检查群是否存在
        Group group = groupMapper.selectById(request.getGroupId());
        if (group == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "群聊不存在");
        }
        
        // 检查是否是群成员（只有群成员才能邀请）
        LambdaQueryWrapper<GroupMember> checkWrapper = new LambdaQueryWrapper<>();
        checkWrapper.eq(GroupMember::getGroupId, request.getGroupId())
                    .eq(GroupMember::getUserId, userId);
        if (groupMemberMapper.selectCount(checkWrapper) == 0) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "你不是群成员");
        }
        
        // 检查用户是否已在群中
        LambdaQueryWrapper<GroupMember> existingWrapper = new LambdaQueryWrapper<>();
        existingWrapper.eq(GroupMember::getGroupId, request.getGroupId())
                       .eq(GroupMember::getUserId, request.getUserId());
        if (groupMemberMapper.selectCount(existingWrapper) > 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "用户已在群中");
        }
        
        // 添加成员
        GroupMember member = new GroupMember();
        member.setGroupId(request.getGroupId());
        member.setUserId(request.getUserId());
        member.setRole(0);
        member.setJoinTime(LocalDateTime.now());
        groupMemberMapper.insert(member);
        
        // 更新群成员数
        group.setMemberCount(group.getMemberCount() + 1);
        groupMapper.updateById(group);
    }

    @Override
    @Transactional
    public void quitGroup(Long userId, Long groupId) {
        Group group = groupMapper.selectById(groupId);
        if (group == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "群聊不存在");
        }
        
        // 群主不能退群，只能解散
        if (group.getOwnerId().equals(userId)) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "群主不能退群，请先转让群主或解散群聊");
        }
        
        // 删除成员
        LambdaQueryWrapper<GroupMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GroupMember::getGroupId, groupId)
               .eq(GroupMember::getUserId, userId);
        groupMemberMapper.delete(wrapper);
        
        // 更新群成员数
        group.setMemberCount(group.getMemberCount() - 1);
        groupMapper.updateById(group);
    }

    @Override
    @Transactional
    public void disbandGroup(Long userId, Long groupId) {
        Group group = groupMapper.selectById(groupId);
        if (group == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "群聊不存在");
        }
        
        // 检查是否是群主
        if (!group.getOwnerId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "只有群主可以解散群聊");
        }
        
        // 删除所有成员
        LambdaQueryWrapper<GroupMember> memberWrapper = new LambdaQueryWrapper<>();
        memberWrapper.eq(GroupMember::getGroupId, groupId);
        groupMemberMapper.delete(memberWrapper);
        
        // 删除群聊
        groupMapper.deleteById(groupId);
    }

    @Override
    public void clearUnreadCount(Long userId, Long groupId) {
        groupMemberMapper.clearUnreadCount(groupId, userId);
    }

    @Override
    @Transactional
    public void updateNotice(Long userId, Long groupId, String notice) {
        Group group = groupMapper.selectById(groupId);
        if (group == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "群聊不存在");
        }

        // 检查是否是群主或管理员
        LambdaQueryWrapper<GroupMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GroupMember::getGroupId, groupId)
                .eq(GroupMember::getUserId, userId);
        GroupMember member = groupMemberMapper.selectOne(wrapper);

        if (member == null || (member.getRole() != 2 && member.getRole() != 1)) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "无权限修改群公告");
        }

        group.setNotice(notice);
        groupMapper.updateById(group);
    }
}