package com.cleevio.vexl.module.group.service;

import com.cleevio.vexl.common.constant.ModuleLockNamespace;
import com.cleevio.vexl.common.integration.firebase.service.DeeplinkService;
import com.cleevio.vexl.common.service.AdvisoryLockService;
import com.cleevio.vexl.module.contact.service.ContactService;
import com.cleevio.vexl.module.file.service.ImageService;
import com.cleevio.vexl.module.group.dto.GroupModel;
import com.cleevio.vexl.module.group.dto.MemberModel;
import com.cleevio.vexl.module.group.dto.mapper.GroupMapper;
import com.cleevio.vexl.module.group.dto.request.CreateGroupRequest;
import com.cleevio.vexl.module.group.dto.request.ExpiredGroupsRequest;
import com.cleevio.vexl.module.group.dto.request.JoinGroupRequest;
import com.cleevio.vexl.module.group.dto.request.LeaveGroupRequest;
import com.cleevio.vexl.module.group.dto.request.MemberRequest;
import com.cleevio.vexl.module.group.entity.Group;
import com.cleevio.vexl.module.group.constant.GroupAdvisoryLock;
import com.cleevio.vexl.module.group.event.GroupCreatedEvent;
import com.cleevio.vexl.module.group.event.GroupJoinRequestedEvent;
import com.cleevio.vexl.module.group.event.GroupLeftEvent;
import com.cleevio.vexl.module.group.exception.GroupNotFoundException;
import com.cleevio.vexl.module.group.util.CodeUtil;
import com.cleevio.vexl.module.group.util.QrCodeUtil;
import com.cleevio.vexl.module.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@Validated
@RequiredArgsConstructor
public class GroupService {

    private final AdvisoryLockService advisoryLockService;
    private final ContactService contactService;
    private final ImageService imageService;
    private final GroupRepository groupRepository;
    private final GroupMapper groupMapper;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final DeeplinkService deeplinkService;

    @Transactional
    public Group createGroup(final User user, @Valid final CreateGroupRequest request) {
        advisoryLockService.lock(
                ModuleLockNamespace.GROUP,
                GroupAdvisoryLock.CREATE_GROUP.name()
        );

        Group group = groupMapper.mapSingleToGroup(request);
        group.setCode(CodeUtil.generateQRCode());
        group.setCreatedBy(user.getPublicKey());

        if (request.logo() != null) {
            final String destination = this.imageService.save(request.logo());
            group.setLogoUrl(destination);
        }

        group.setQrCodeUrl(createQrCodeUrl(group.getCode()));

        final Group savedGroup = this.groupRepository.save(group);
        applicationEventPublisher.publishEvent(new GroupCreatedEvent(savedGroup.getUuid(), user));
        return savedGroup;
    }

    @Transactional
    public void joinGroup(final User user, @Valid final JoinGroupRequest request) {
        advisoryLockService.lock(
                ModuleLockNamespace.GROUP,
                GroupAdvisoryLock.JOIN_GROUP.name()
        );

        final String groupUuid = this.groupRepository.findGroupUuidByCode(request.code())
                .orElseThrow(GroupNotFoundException::new);

        applicationEventPublisher.publishEvent(new GroupJoinRequestedEvent(groupUuid, user));
    }

    @Transactional(readOnly = true)
    public List<GroupModel> retrieveMyGroups(final User user) {
        final List<String> userGroupUuid = this.contactService.getGroupsUuidsByHash(user.getHash());

        return createGroupModelMapWithMemberCount(
                this.groupRepository.findGroupsByUuids(userGroupUuid));
    }

    public void leaveGroup(final User user, final LeaveGroupRequest request) {
        applicationEventPublisher.publishEvent(new GroupLeftEvent(user.getHash(), request.groupUuid()));
    }

    @Transactional(readOnly = true)
    public GroupModel retrieveGroupByCode(final int code) {
        final Group group = this.groupRepository.findGroupsByCode(code)
                .orElseThrow(GroupNotFoundException::new);
        return new GroupModel(
                group,
                contactService.getContactsCountByHashTo(group.getUuid())
        );
    }

    @Transactional(readOnly = true)
    public List<MemberModel> retrieveMembers(@Valid final List<MemberRequest.GroupRequest> groups, final User user) {
        final List<MemberModel> members = new ArrayList<>();

        groups.forEach(group -> {
            final String groupUuid = group.groupUuid();
            final List<String> publicKeys = new ArrayList<>(List.of(user.getPublicKey()));
            publicKeys.addAll(group.publicKeys());
            members.add(
                    new MemberModel(
                            groupUuid,
                            this.contactService.retrieveNewGroupMembers(groupUuid, publicKeys),
                            this.contactService.retrieveRemovedGroupMembers(groupUuid, publicKeys)
                    )
            );
        });

        return members;
    }

    @Transactional(readOnly = true)
    public List<GroupModel> retrieveExpiredGroups(@Valid final ExpiredGroupsRequest request) {
        return createGroupModelMapWithMemberCount(this.groupRepository.retrieveExpiredGroups(request.uuids()));
    }

    private List<GroupModel> createGroupModelMapWithMemberCount(List<Group> groups) {
        final List<GroupModel> groupModels = new ArrayList<>();
        groups.forEach(g -> groupModels.add(new GroupModel(g, contactService.getContactsCountByHashTo(g.getUuid()))));
        return groupModels;
    }

    private String createQrCodeUrl(final int code) {
        final String dynamicLink = deeplinkService.createDynamicLink(String.valueOf(code));

        return this.imageService.save(QrCodeUtil.getQRCodeImageRequest(dynamicLink));
    }
}
