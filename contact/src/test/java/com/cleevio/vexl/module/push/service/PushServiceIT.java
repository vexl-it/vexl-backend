package com.cleevio.vexl.module.push.service;

import com.cleevio.vexl.common.IntegrationTest;
import com.cleevio.vexl.common.integration.firebase.service.FirebaseService;
import com.cleevio.vexl.module.group.entity.Group;
import com.cleevio.vexl.module.group.service.GroupService;
import com.cleevio.vexl.module.push.entity.Push;
import com.cleevio.vexl.module.user.dto.request.CreateUserRequest;
import com.cleevio.vexl.module.user.entity.User;
import com.cleevio.vexl.module.user.service.UserService;
import com.cleevio.vexl.util.CreateRequestTestUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@IntegrationTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PushServiceIT {

    private final static String PUBLIC_KEY_USER_1 = "dummy_public_key_1";
    private final static String HASH_USER_1 = "dummy_hash_1";
    private final static String FIREBASE_TOKEN_1 = "dummy_firebase_token_1";

    private final static String PUBLIC_KEY_USER_2 = "dummy_public_key_2";
    private final static String HASH_USER_2 = "dummy_hash_2";
    private final static String FIREBASE_TOKEN_2 = "dummy_firebase_token_2";

    private final static String PUBLIC_KEY_USER_3 = "dummy_public_key_3";
    private final static String HASH_USER_3 = "dummy_hash_3";
    private final static String FIREBASE_TOKEN_3 = "dummy_firebase_token_3";
    private final static String FAKE_GROUP_UUID = "dummy_fake_group_uuid";
    private final static String[] FIREBASE_TOKENS_1 = {"0", "1", "2"};
    private final static String[] FIREBASE_TOKENS_2 = {"0", "1", "2", "3", "4"};
    private final static String[] FIREBASE_TOKENS_3 = {"0", "1", "2", "3", "4", "5"};
    private final static String[] FIREBASE_TOKENS_4 = {"0", "1", "6"};
    private final static String DYNAMIC_LINK = "dummy_dynamic_link";
    private final PushService pushService;
    private final PushRepository pushRepository;
    private final GroupService groupService;
    private final UserService userService;

    @MockBean
    private FirebaseService firebaseService;

    @BeforeEach
    void beforeEach() {
        when(firebaseService.createDynamicLink(any())).thenReturn(DYNAMIC_LINK);
    }

    @Autowired
    public PushServiceIT(PushService pushService, PushRepository pushRepository,
                         GroupService groupService, UserService userService) {
        this.pushService = pushService;
        this.pushRepository = pushRepository;
        this.groupService = groupService;
        this.userService = userService;
    }

    @Test
    void testCreatePushNotification_shouldBeCreated() {
        final User user1 = userService.createUser(PUBLIC_KEY_USER_1, HASH_USER_1, new CreateUserRequest(FIREBASE_TOKEN_1));
        final User user2 = userService.createUser(PUBLIC_KEY_USER_2, HASH_USER_2, new CreateUserRequest(FIREBASE_TOKEN_2));
        final User user3 = userService.createUser(PUBLIC_KEY_USER_3, HASH_USER_3, new CreateUserRequest(FIREBASE_TOKEN_3));
        final Group group = this.groupService.createGroup(user1, CreateRequestTestUtil.createCreateGroupRequest());

        // User created group and joined, no need for notifications
        final List<Push> result1 = this.pushRepository.findAll();
        assertThat(result1).hasSize(0);

        this.groupService.joinGroup(user2, CreateRequestTestUtil.createJoinGroupRequest(group.getCode()));

        // User joined, we need notify first user about new member
        final List<Push> result2 = this.pushRepository.findAll();
        assertThat(result2).hasSize(1);
        assertThat(result2.get(0).getFirebaseTokens()).containsOnly(FIREBASE_TOKEN_1);

        this.groupService.joinGroup(user3, CreateRequestTestUtil.createJoinGroupRequest(group.getCode()));

        // Notification job didn't run and next user has joined the group. So we should have two notifications.
        final List<Push> result3 = this.pushRepository.findAll();
        assertThat(result3).hasSize(2);
        assertThat(result3.get(0).getFirebaseTokens()).containsOnly(FIREBASE_TOKEN_1);
        assertThat(result3.get(1).getFirebaseTokens()).containsOnly(FIREBASE_TOKEN_1, FIREBASE_TOKEN_2);

        // Simulate job run
        final Map<String, Set<String>> notifications = this.pushService.processPushNotification();
        assertThat(notifications).hasSize(1);
        assertThat(notifications.get(group.getUuid())).hasSize(2);
        assertThat(notifications.get(group.getUuid())).containsOnly(FIREBASE_TOKEN_1, FIREBASE_TOKEN_2);

        // All notifications should be deleted
        final List<Push> result4 = this.pushRepository.findAll();
        assertThat(result4).hasSize(0);
    }

    @Test
    void testNotificationCreation_forEachGroupUuidAllFirebaseTokens() {
        final User user1 = userService.createUser(PUBLIC_KEY_USER_1, HASH_USER_1, new CreateUserRequest(FIREBASE_TOKEN_1));
        final Group group1 = this.groupService.createGroup(user1, CreateRequestTestUtil.createCreateGroupRequest());
        final Group group2 = this.groupService.createGroup(user1, CreateRequestTestUtil.createCreateGroupRequest());

        this.pushRepository.save(new Push(group1.getUuid(), Set.of(FIREBASE_TOKENS_1)));
        this.pushRepository.save(new Push(group1.getUuid(), Set.of(FIREBASE_TOKENS_2)));
        this.pushRepository.save(new Push(group2.getUuid(), Set.of(FIREBASE_TOKENS_3)));
        this.pushRepository.save(new Push(group2.getUuid(), Set.of(FIREBASE_TOKENS_4)));

        // Simulate job run
        final Map<String, Set<String>> notifications = this.pushService.processPushNotification();
        assertThat(notifications).hasSize(2);
        assertThat(notifications.get(group1.getUuid())).doesNotHaveDuplicates();
        assertThat(notifications.get(group1.getUuid())).contains(FIREBASE_TOKENS_1);
        assertThat(notifications.get(group1.getUuid())).contains(FIREBASE_TOKENS_2);
        assertThat(notifications.get(group2.getUuid())).doesNotHaveDuplicates();
        assertThat(notifications.get(group2.getUuid())).contains(FIREBASE_TOKENS_3);
        assertThat(notifications.get(group2.getUuid())).contains(FIREBASE_TOKENS_4);

        // All notifications should be deleted
        final List<Push> result4 = this.pushRepository.findAll();
        assertThat(result4).hasSize(0);
    }

    @Test
    void testSendNotificationToNotExistingGroup_shouldNotSentNotificationToNotExistingGroup() {
        this.pushRepository.save(new Push(FAKE_GROUP_UUID, Set.of(FIREBASE_TOKENS_4)));
        final Map<String, Set<String>> notifications = this.pushService.processPushNotification();
        assertThat(notifications).hasSize(0);

        // All notifications should be deleted
        final List<Push> result = this.pushRepository.findAll();
        assertThat(result).hasSize(0);
    }


}
