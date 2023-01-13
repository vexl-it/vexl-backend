package com.cleevio.vexl.module.group.service;

import com.cleevio.vexl.common.IntegrationTest;
import com.cleevio.vexl.common.integration.firebase.service.FirebaseService;
import com.cleevio.vexl.module.group.dto.GroupModel;
import com.cleevio.vexl.module.group.dto.MemberModel;
import com.cleevio.vexl.module.group.dto.request.CreateGroupRequest;
import com.cleevio.vexl.module.group.dto.request.ExpiredGroupsRequest;
import com.cleevio.vexl.module.group.dto.request.LeaveGroupRequest;
import com.cleevio.vexl.module.group.dto.request.MemberRequest;
import com.cleevio.vexl.module.group.entity.Group;
import com.cleevio.vexl.module.group.exception.GroupNotFoundException;
import com.cleevio.vexl.module.user.entity.User;
import com.cleevio.vexl.module.user.service.UserService;
import com.cleevio.vexl.util.CreateRequestTestUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@IntegrationTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class GroupServiceIT {

    private final static String PUBLIC_KEY_USER_1 = "dummy_public_key";
    private final static String HASH_USER_1 = "dummy_hash";
    private final static String PUBLIC_KEY_USER_2 = "dummy_public_key_2";
    private final static String HASH_USER_2 = "dummy_hash_2";
    private final static String PUBLIC_KEY_USER_3 = "dummy_public_key_3";
    private final static String HASH_USER_3 = "dummy_hash_3";
    private final static String DYNAMIC_LINK = "dummy_dynamic_link";
    private final GroupService groupService;
    private final UserService userService;
    private final GroupRepository groupRepository;

    @MockBean
    private FirebaseService firebaseService;

    @BeforeEach
    void beforeEach() {
        when(firebaseService.createDynamicLink(any())).thenReturn(DYNAMIC_LINK);
    }

    @Autowired
    public GroupServiceIT(GroupService groupService, UserService userService, GroupRepository groupRepository) {
        this.groupService = groupService;
        this.userService = userService;
        this.groupRepository = groupRepository;
    }

    @Test
    void testCreateGroup_shouldBeCreated() {
        final User user = userService.createUser(PUBLIC_KEY_USER_1, HASH_USER_1);
        final CreateGroupRequest createGroupRequest = CreateRequestTestUtil.createCreateGroupRequest();
        final Group group = this.groupService.createGroup(user, createGroupRequest);

        assertThat(group.getUuid()).isNotBlank();
        assertThat(group.getName()).isEqualTo(createGroupRequest.name());
        assertThat(group.getLogoUrl()).isEqualTo(createGroupRequest.logo());
        assertThat(group.getExpirationAt()).isEqualTo(createGroupRequest.expirationAt());
        assertThat(group.getClosureAt()).isEqualTo(createGroupRequest.closureAt());
        assertThat(group.getCreatedAt()).isNotNull();
        assertThat(group.getCreatedBy()).isEqualTo(PUBLIC_KEY_USER_1);
    }

    @Test
    void testJoinGroupAndRetrieveMyGroups_shouldBeJoinedAndGroupShouldBeFound() {
        final User user = userService.createUser(PUBLIC_KEY_USER_1, HASH_USER_1);
        final User user2 = userService.createUser(PUBLIC_KEY_USER_2, HASH_USER_2);

        final List<GroupModel> groupsBefore = this.groupService.retrieveMyGroups(user);
        assertThat(groupsBefore).hasSize(0);

        final Group group = this.groupService.createGroup(user2, CreateRequestTestUtil.createCreateGroupRequest());
        this.groupService.joinGroup(user, CreateRequestTestUtil.createJoinGroupRequest(group.getCode()));

        final List<GroupModel> groups = this.groupService.retrieveMyGroups(user);

        assertThat(groups).hasSize(1);

        final Group joinedGroup = groups.get(0).group();
        assertThat(joinedGroup.getName()).isEqualTo(group.getName());
        assertThat(joinedGroup.getUuid()).isEqualTo(group.getUuid());
        assertThat(joinedGroup.getCode()).isEqualTo(group.getCode());
        assertThat(joinedGroup.getLogoUrl()).isEqualTo(group.getLogoUrl());
        assertThat(joinedGroup.getCreatedAt()).isEqualTo(group.getCreatedAt());
        assertThat(joinedGroup.getExpirationAt()).isEqualTo(group.getExpirationAt());
        assertThat(joinedGroup.getClosureAt()).isEqualTo(group.getClosureAt());
        assertThat(joinedGroup.getCreatedBy()).isEqualTo(group.getCreatedBy());
    }

    @Test
    void testJoinGroup_shouldReturnGroupNotFoundException() {
        final User user = userService.createUser(PUBLIC_KEY_USER_1, HASH_USER_1);
        final var qrCode = 111256;

        assertThrows(
                GroupNotFoundException.class,
                () -> this.groupService.joinGroup(user, CreateRequestTestUtil.createJoinGroupRequest(qrCode))
        );
    }

    @Test
    void testRetrieveMyGroups_shouldFindAllMyGroups() {
        final User user = userService.createUser(PUBLIC_KEY_USER_1, HASH_USER_1);
        final User user2 = userService.createUser(PUBLIC_KEY_USER_2, HASH_USER_2);
        final Group group = this.groupService.createGroup(user, CreateRequestTestUtil.createCreateGroupRequest());
        final Group group2 = this.groupService.createGroup(user2, CreateRequestTestUtil.createCreateGroupRequest());
        final Group group3 = this.groupService.createGroup(user, CreateRequestTestUtil.createCreateGroupRequest());
        final Group group4 = this.groupService.createGroup(user, CreateRequestTestUtil.createCreateGroupRequest());

        this.groupService.joinGroup(user2, CreateRequestTestUtil.createJoinGroupRequest(group.getCode()));
        this.groupService.joinGroup(user, CreateRequestTestUtil.createJoinGroupRequest(group2.getCode()));
        this.groupService.joinGroup(user, CreateRequestTestUtil.createJoinGroupRequest(group3.getCode()));
        this.groupService.joinGroup(user2, CreateRequestTestUtil.createJoinGroupRequest(group4.getCode()));

        // If user creates a group, he is automatically connected to it.

        final List<GroupModel> user1Groups = this.groupService.retrieveMyGroups(user);
        final List<GroupModel> user2Groups = this.groupService.retrieveMyGroups(user2);

        assertThat(user1Groups).hasSize(4);
        assertThat(user2Groups).hasSize(3);
    }

    @Test
    void testLeaveGroup_shouldLeaveGroup() {
        final User user = userService.createUser(PUBLIC_KEY_USER_1, HASH_USER_1);
        final Group group1 = this.groupService.createGroup(user, CreateRequestTestUtil.createCreateGroupRequest());
        final Group group2 = this.groupService.createGroup(user, CreateRequestTestUtil.createCreateGroupRequest());

        final List<GroupModel> groupsBeforeLeave = this.groupService.retrieveMyGroups(user);

        this.groupService.leaveGroup(user, new LeaveGroupRequest(group1.getUuid()));

        final List<GroupModel> groupsAfterLeave = this.groupService.retrieveMyGroups(user);

        assertThat(groupsBeforeLeave).hasSize(2);
        assertThat(groupsAfterLeave).hasSize(1);

        // leave group 2 as well
        this.groupService.leaveGroup(user, new LeaveGroupRequest(group2.getUuid()));

        assertThat(this.groupService.retrieveMyGroups(user)).hasSize(0);
    }

    @Test
    void testRetrieveGroupsByUuid_shouldBeRetrieved() {
        final User user = userService.createUser(PUBLIC_KEY_USER_1, HASH_USER_1);
        final Group group1 = this.groupService.createGroup(user, CreateRequestTestUtil.createCreateGroupRequest());
        this.groupService.createGroup(user, CreateRequestTestUtil.createCreateGroupRequest());

        final List<Group> groups = this.groupRepository.findAll();
        assertThat(groups).hasSize(2);

        final Group group = this.groupService.retrieveGroupByCode(group1.getCode()).group();
        assertThat(group.getUuid()).isEqualTo(group1.getUuid());
        assertThat(group.getName()).isEqualTo(group1.getName());
        assertThat(group.getCode()).isEqualTo(group1.getCode());
        assertThat(group.getExpirationAt()).isEqualTo(group1.getExpirationAt());
        assertThat(group.getClosureAt()).isEqualTo(group1.getClosureAt());
        assertThat(group.getCreatedBy()).isEqualTo(group1.getCreatedBy());
        assertThat(group.getCreatedAt()).isEqualTo(group1.getCreatedAt());
    }

    @Test
    void testRetrieveNewMembers_shouldBeRetrieved() {
        final User user1 = userService.createUser(PUBLIC_KEY_USER_1, HASH_USER_1);
        final User user2 = userService.createUser(PUBLIC_KEY_USER_2, HASH_USER_2);
        final Group group = this.groupService.createGroup(user1, CreateRequestTestUtil.createCreateGroupRequest());

        //no new user
        final List<MemberModel> emptyResult = this.groupService.retrieveMembers(CreateRequestTestUtil.createGroupRequestList(group.getUuid(), Collections.emptyList()), user1);
        assertThat(emptyResult.get(0).newPublicKeys().size()).isEqualTo(0);

        //a new user join group
        this.groupService.joinGroup(user2, CreateRequestTestUtil.createJoinGroupRequest(group.getCode()));

        final List<MemberModel> oneResult = this.groupService.retrieveMembers(CreateRequestTestUtil.createGroupRequestList(group.getUuid(), Collections.emptyList()), user1);
        assertThat(oneResult.size()).isEqualTo(1);
        assertThat(oneResult.get(0).newPublicKeys().get(0)).isEqualTo(PUBLIC_KEY_USER_2);

        //I know about a new user now
        final List<MemberModel> emptyResult2 = this.groupService.retrieveMembers(CreateRequestTestUtil.createGroupRequestList(group.getUuid(), List.of(user2.getPublicKey())), user1);
        assertThat(emptyResult2.get(0).newPublicKeys().size()).isEqualTo(0);
    }

    @Test
    void testRetrieveNewMembersWithMoreGroups_shouldBeRetrieved() {
        final User user1 = userService.createUser(PUBLIC_KEY_USER_1, HASH_USER_1);
        final User user2 = userService.createUser(PUBLIC_KEY_USER_2, HASH_USER_2);
        final User user3 = userService.createUser(PUBLIC_KEY_USER_3, HASH_USER_3);
        final Group group1 = this.groupService.createGroup(user1, CreateRequestTestUtil.createCreateGroupRequest());
        final Group group2 = this.groupService.createGroup(user1, CreateRequestTestUtil.createCreateGroupRequest());
        final Group group3 = this.groupService.createGroup(user1, CreateRequestTestUtil.createCreateGroupRequest());
        final Group group4 = this.groupService.createGroup(user1, CreateRequestTestUtil.createCreateGroupRequest());
        final List<MemberRequest.GroupRequest> groupRequests = new ArrayList<>();

        this.groupService.joinGroup(user2, CreateRequestTestUtil.createJoinGroupRequest(group1.getCode()));
        this.groupService.joinGroup(user2, CreateRequestTestUtil.createJoinGroupRequest(group2.getCode()));
        this.groupService.joinGroup(user2, CreateRequestTestUtil.createJoinGroupRequest(group3.getCode()));

        this.groupService.joinGroup(user3, CreateRequestTestUtil.createJoinGroupRequest(group3.getCode()));
        this.groupService.joinGroup(user3, CreateRequestTestUtil.createJoinGroupRequest(group4.getCode()));

        //everyone is unknown
        groupRequests.add(CreateRequestTestUtil.createGroupRequest(group1.getUuid(), Collections.emptyList()));
        groupRequests.add(CreateRequestTestUtil.createGroupRequest(group2.getUuid(), Collections.emptyList()));
        groupRequests.add(CreateRequestTestUtil.createGroupRequest(group3.getUuid(), Collections.emptyList()));
        groupRequests.add(CreateRequestTestUtil.createGroupRequest(group4.getUuid(), Collections.emptyList()));

        final List<MemberModel> allUsersResult = this.groupService.retrieveMembers(groupRequests, user1);
        assertThat(allUsersResult.get(0).groupUuid()).isEqualTo(group1.getUuid());
        assertThat(allUsersResult.get(0).newPublicKeys().size()).isEqualTo(1);
        assertThat(allUsersResult.get(0).newPublicKeys().get(0)).isEqualTo(PUBLIC_KEY_USER_2);

        assertThat(allUsersResult.get(1).groupUuid()).isEqualTo(group2.getUuid());
        assertThat(allUsersResult.get(1).newPublicKeys().size()).isEqualTo(1);
        assertThat(allUsersResult.get(1).newPublicKeys().get(0)).isEqualTo(PUBLIC_KEY_USER_2);

        assertThat(allUsersResult.get(2).groupUuid()).isEqualTo(group3.getUuid());
        assertThat(allUsersResult.get(2).newPublicKeys().size()).isEqualTo(2);
        assertThat(allUsersResult.get(2).newPublicKeys()).contains(PUBLIC_KEY_USER_2, PUBLIC_KEY_USER_3);

        assertThat(allUsersResult.get(3).groupUuid()).isEqualTo(group4.getUuid());
        assertThat(allUsersResult.get(3).newPublicKeys().size()).isEqualTo(1);
        assertThat(allUsersResult.get(3).newPublicKeys().get(0)).isEqualTo(PUBLIC_KEY_USER_3);

        //User 2 is known
        groupRequests.clear();
        groupRequests.add(CreateRequestTestUtil.createGroupRequest(group1.getUuid(), List.of(PUBLIC_KEY_USER_2)));
        groupRequests.add(CreateRequestTestUtil.createGroupRequest(group2.getUuid(), List.of(PUBLIC_KEY_USER_2)));
        groupRequests.add(CreateRequestTestUtil.createGroupRequest(group3.getUuid(), List.of(PUBLIC_KEY_USER_2)));
        groupRequests.add(CreateRequestTestUtil.createGroupRequest(group4.getUuid(), Collections.emptyList()));

        final List<MemberModel> user2IsKnown = this.groupService.retrieveMembers(groupRequests, user1);
        assertThat(user2IsKnown.get(0).newPublicKeys().size()).isEqualTo(0);

        assertThat(user2IsKnown.get(1).newPublicKeys().size()).isEqualTo(0);

        assertThat(user2IsKnown.get(2).groupUuid()).isEqualTo(group3.getUuid());
        assertThat(user2IsKnown.get(2).newPublicKeys().size()).isEqualTo(1);
        assertThat(user2IsKnown.get(2).newPublicKeys().get(0)).isEqualTo(PUBLIC_KEY_USER_3);

        assertThat(user2IsKnown.get(3).groupUuid()).isEqualTo(group4.getUuid());
        assertThat(user2IsKnown.get(3).newPublicKeys().size()).isEqualTo(1);
        assertThat(user2IsKnown.get(3).newPublicKeys().get(0)).isEqualTo(PUBLIC_KEY_USER_3);

        //User 3 is known
        groupRequests.clear();
        groupRequests.add(CreateRequestTestUtil.createGroupRequest(group1.getUuid(), Collections.emptyList()));
        groupRequests.add(CreateRequestTestUtil.createGroupRequest(group2.getUuid(), Collections.emptyList()));
        groupRequests.add(CreateRequestTestUtil.createGroupRequest(group3.getUuid(), List.of(PUBLIC_KEY_USER_3)));
        groupRequests.add(CreateRequestTestUtil.createGroupRequest(group4.getUuid(), List.of(PUBLIC_KEY_USER_3)));

        final List<MemberModel> user3IsKnown = this.groupService.retrieveMembers(groupRequests, user1);
        assertThat(user3IsKnown.get(0).groupUuid()).isEqualTo(group1.getUuid());
        assertThat(user3IsKnown.get(0).newPublicKeys().size()).isEqualTo(1);
        assertThat(user3IsKnown.get(0).newPublicKeys().get(0)).isEqualTo(PUBLIC_KEY_USER_2);

        assertThat(user3IsKnown.get(1).groupUuid()).isEqualTo(group2.getUuid());
        assertThat(user3IsKnown.get(1).newPublicKeys().size()).isEqualTo(1);
        assertThat(user3IsKnown.get(1).newPublicKeys().get(0)).isEqualTo(PUBLIC_KEY_USER_2);

        assertThat(user3IsKnown.get(2).groupUuid()).isEqualTo(group3.getUuid());
        assertThat(user3IsKnown.get(2).newPublicKeys().size()).isEqualTo(1);
        assertThat(user3IsKnown.get(2).newPublicKeys().get(0)).isEqualTo(PUBLIC_KEY_USER_2);

        assertThat(user3IsKnown.get(3).groupUuid()).isEqualTo(group4.getUuid());
        assertThat(user3IsKnown.get(3).newPublicKeys().size()).isEqualTo(0);

        //everyone is known
        groupRequests.clear();
        groupRequests.add(CreateRequestTestUtil.createGroupRequest(group1.getUuid(), List.of(PUBLIC_KEY_USER_2)));
        groupRequests.add(CreateRequestTestUtil.createGroupRequest(group2.getUuid(), List.of(PUBLIC_KEY_USER_2)));
        groupRequests.add(CreateRequestTestUtil.createGroupRequest(group3.getUuid(), List.of(PUBLIC_KEY_USER_2, PUBLIC_KEY_USER_3)));
        groupRequests.add(CreateRequestTestUtil.createGroupRequest(group4.getUuid(), List.of(PUBLIC_KEY_USER_3)));

        final List<MemberModel> allIsKnown = this.groupService.retrieveMembers(groupRequests, user1);
        assertThat(allIsKnown.get(0).newPublicKeys().size()).isEqualTo(0);
        assertThat(allIsKnown.get(1).newPublicKeys().size()).isEqualTo(0);
        assertThat(allIsKnown.get(2).newPublicKeys().size()).isEqualTo(0);
        assertThat(allIsKnown.get(3).newPublicKeys().size()).isEqualTo(0);
    }

    @Test
    void testFindExpiredGroups_shouldBeFound() {
        final User user = userService.createUser(PUBLIC_KEY_USER_1, HASH_USER_1);
        final Group expiredGroup1 = this.groupService.createGroup(user, CreateRequestTestUtil.createCreateGroupRequestExpired());
        final Group nonExpiredGroup1 = this.groupService.createGroup(user, CreateRequestTestUtil.createCreateGroupRequest());
        final Group nonExpiredGroup2 = this.groupService.createGroup(user, CreateRequestTestUtil.createCreateGroupRequest());
        final Group expiredGroup2 = this.groupService.createGroup(user, CreateRequestTestUtil.createCreateGroupRequestExpired());
        final List<String> groupUuids = List.of(expiredGroup1.getUuid(), nonExpiredGroup1.getUuid(), expiredGroup2.getUuid(), nonExpiredGroup2.getUuid());

        final List<GroupModel> expiredGroups = this.groupService.retrieveExpiredGroups(new ExpiredGroupsRequest(groupUuids));

        assertThat(expiredGroups).hasSize(2);
        assertThat(expiredGroups.get(0).group().getUuid()).doesNotContain(nonExpiredGroup1.getUuid(), nonExpiredGroup2.getUuid());
        assertThat(expiredGroups.get(1).group().getUuid()).doesNotContain(nonExpiredGroup1.getUuid(), nonExpiredGroup2.getUuid());
    }

    @Test
    void testRetrieveRemovedGroupMembers_shouldBeRetrieved() {
        final User user1 = userService.createUser(PUBLIC_KEY_USER_1, HASH_USER_1);
        final User user2 = userService.createUser(PUBLIC_KEY_USER_2, HASH_USER_2);
        final User user3 = userService.createUser(PUBLIC_KEY_USER_3, HASH_USER_3);
        final Group group = this.groupService.createGroup(user1, CreateRequestTestUtil.createCreateGroupRequest());
        final List<MemberRequest.GroupRequest> groupRequests = new ArrayList<>();

        this.groupService.joinGroup(user2, CreateRequestTestUtil.createJoinGroupRequest(group.getCode()));
        this.groupService.joinGroup(user3, CreateRequestTestUtil.createJoinGroupRequest(group.getCode()));

        //everyone is unknown
        groupRequests.add(CreateRequestTestUtil.createGroupRequest(group.getUuid(), Collections.emptyList()));

        // Check all members are in group
        final List<MemberModel> members = this.groupService.retrieveMembers(groupRequests, user1);
        assertThat(members.get(0).newPublicKeys()).hasSize(2);
        assertThat(members.get(0).newPublicKeys()).contains(PUBLIC_KEY_USER_2, PUBLIC_KEY_USER_3);
        assertThat(members.get(0).removedPublicKeys()).hasSize(0);

        // User 2 is leaving group
        groupRequests.clear();
        groupRequests.add(CreateRequestTestUtil.createGroupRequest(group.getUuid(), List.of(PUBLIC_KEY_USER_2, PUBLIC_KEY_USER_3)));

        this.groupService.leaveGroup(user2, new LeaveGroupRequest(group.getUuid()));
        final List<MemberModel> membersAfterUser2Left = this.groupService.retrieveMembers(groupRequests, user1);
        assertThat(membersAfterUser2Left.get(0).newPublicKeys()).hasSize(0);
        assertThat(membersAfterUser2Left.get(0).removedPublicKeys()).hasSize(1);
        assertThat(membersAfterUser2Left.get(0).removedPublicKeys()).contains(PUBLIC_KEY_USER_2);

        // User 3 is leaving group
        this.groupService.leaveGroup(user3, new LeaveGroupRequest(group.getUuid()));
        final List<MemberModel> membersAfterUser2AndUser3Left = this.groupService.retrieveMembers(groupRequests, user1);
        assertThat(membersAfterUser2AndUser3Left.get(0).newPublicKeys()).hasSize(0);
        assertThat(membersAfterUser2AndUser3Left.get(0).removedPublicKeys()).hasSize(2);
        assertThat(membersAfterUser2AndUser3Left.get(0).removedPublicKeys()).contains(PUBLIC_KEY_USER_2, PUBLIC_KEY_USER_3);

        // User 2 and User 3 are joining group again
        this.groupService.joinGroup(user2, CreateRequestTestUtil.createJoinGroupRequest(group.getCode()));
        this.groupService.joinGroup(user3, CreateRequestTestUtil.createJoinGroupRequest(group.getCode()));
        final List<MemberModel> membersAfterRejoinUser2AndUser3 = this.groupService.retrieveMembers(groupRequests, user1);
        assertThat(membersAfterRejoinUser2AndUser3.get(0).newPublicKeys()).hasSize(0);
        assertThat(membersAfterRejoinUser2AndUser3.get(0).removedPublicKeys()).hasSize(0);
    }
}
