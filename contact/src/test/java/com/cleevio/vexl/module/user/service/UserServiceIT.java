package com.cleevio.vexl.module.user.service;

import com.cleevio.vexl.common.IntegrationTest;
import com.cleevio.vexl.module.user.constant.Platform;
import com.cleevio.vexl.module.user.dto.request.RefreshUserRequest;
import com.cleevio.vexl.module.user.entity.User;
import com.cleevio.vexl.util.CreateRequestTestUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@IntegrationTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserServiceIT {

    private final static String PUBLIC_KEY_USER_1 = "dummy_public_key";

    private final static String PUBLIC_KEY_USER_2 = "dummy_public_key_2";
    private final static String HASH_USER_1 = "dummy_hash_1";
    private final static String HASH_USER_2 = "dummy_hash_2";
    private final static String FIREBASE_TOKEN_1 = "dummy_firebase_token_1";
    private final static String FIREBASE_TOKEN_2 = "dummy_firebase_token_2";
    private final UserService userService;
    private final UserRepository userRepository;

    @Autowired
    public UserServiceIT(UserService userService, UserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
    }

    @Test
    void testCreateUser_shouldBeCreated() {
        final User user = this.userService.createUser(PUBLIC_KEY_USER_1, HASH_USER_1);

        final User savedUser = this.userRepository.findById(user.getId()).get();
        final int size = this.userRepository.findAll().size();

        assertThat(size).isEqualTo(1);
        assertThat(savedUser.getPublicKey()).isEqualTo(PUBLIC_KEY_USER_1);
        assertThat(savedUser.getHash()).isEqualTo(HASH_USER_1);
    }

    @Test
    void testCreateUserWithFirebaseToken_shouldBeCreated() {
        final User user = this.userService.createUser(PUBLIC_KEY_USER_1, HASH_USER_1, CreateRequestTestUtil.createCreateUserRequest(FIREBASE_TOKEN_1));

        final User savedUser = this.userRepository.findById(user.getId()).get();
        final int size = this.userRepository.findAll().size();

        assertThat(size).isEqualTo(1);
        assertThat(savedUser.getPublicKey()).isEqualTo(PUBLIC_KEY_USER_1);
        assertThat(savedUser.getHash()).isEqualTo(HASH_USER_1);
        assertThat(savedUser.getFirebaseToken()).isEqualTo(FIREBASE_TOKEN_1);
    }

    @Test
    void testRecreateUser_shouldBeCreated() {
        final User user = this.userService.createUser(PUBLIC_KEY_USER_1, HASH_USER_1);

        final User savedUser = this.userRepository.findById(user.getId()).get();

        assertThat(savedUser.getPublicKey()).isEqualTo(PUBLIC_KEY_USER_1);
        assertThat(savedUser.getHash()).isEqualTo(HASH_USER_1);

        this.userService.createUser(PUBLIC_KEY_USER_2, HASH_USER_1);

        final List<User> allUsers = this.userRepository.findAll();
        assertThat(allUsers).hasSize(1);
        assertThat(allUsers.get(0).getPublicKey()).isEqualTo(PUBLIC_KEY_USER_2);
        assertThat(allUsers.get(0).getHash()).isEqualTo(HASH_USER_1);

    }

    @Test
    void testFindUserByPublicKeyAndHash_shouldBeFound() {
        this.userService.createUser(PUBLIC_KEY_USER_1, HASH_USER_1);

        final User savedUser = this.userService.findByPublicKeyAndHash(PUBLIC_KEY_USER_1, HASH_USER_1).get();

        assertThat(savedUser.getPublicKey()).isEqualTo(PUBLIC_KEY_USER_1);
        assertThat(savedUser.getHash()).isEqualTo(HASH_USER_1);
    }

    @Test
    void testDeleteUser_shouldBeDeleted() {
        this.userService.createUser(PUBLIC_KEY_USER_1, HASH_USER_1);

        final User savedUser = this.userService.findByPublicKeyAndHash(PUBLIC_KEY_USER_1, HASH_USER_1).get();

        assertThat(savedUser.getPublicKey()).isEqualTo(PUBLIC_KEY_USER_1);
        assertThat(savedUser.getHash()).isEqualTo(HASH_USER_1);

        this.userService.removeUserAndContacts(savedUser);

        final List<User> allUsers = this.userRepository.findAll();
        assertThat(allUsers).isEmpty();
    }

    @Test
    void testRefreshUser_shouldBeRefreshed() {
        this.userService.createUser(PUBLIC_KEY_USER_1, HASH_USER_1, CreateRequestTestUtil.createCreateUserRequest(FIREBASE_TOKEN_1));

        this.userService.refreshUser(PUBLIC_KEY_USER_1, HASH_USER_1, Platform.ANDROID, new RefreshUserRequest(true));

        final User user = this.userRepository.findUserByPublicKeyAndHash(PUBLIC_KEY_USER_1, HASH_USER_1).get();
        assertThat(user.getPublicKey()).isEqualTo(PUBLIC_KEY_USER_1);
        assertThat(user.getHash()).isEqualTo(HASH_USER_1);
        assertThat(user.getPlatform()).isEqualTo(Platform.ANDROID);
        assertThat(user.getRefreshedAt()).isToday();
    }

    @Test
    void testRefreshUserAndRemoveRefreshDate_shouldBeRefreshedAndRemoved() {
        this.userService.createUser(PUBLIC_KEY_USER_1, HASH_USER_1, CreateRequestTestUtil.createCreateUserRequest(FIREBASE_TOKEN_1));

        this.userService.refreshUser(PUBLIC_KEY_USER_1, HASH_USER_1, Platform.ANDROID, new RefreshUserRequest(true));

        final User userAfterRefresh = this.userRepository.findUserByPublicKeyAndHash(PUBLIC_KEY_USER_1, HASH_USER_1).get();
        assertThat(userAfterRefresh.getPublicKey()).isEqualTo(PUBLIC_KEY_USER_1);
        assertThat(userAfterRefresh.getHash()).isEqualTo(HASH_USER_1);
        assertThat(userAfterRefresh.getPlatform()).isEqualTo(Platform.ANDROID);
        assertThat(userAfterRefresh.getRefreshedAt()).isToday();

        this.userService.refreshUser(PUBLIC_KEY_USER_1, HASH_USER_1, Platform.ANDROID, new RefreshUserRequest(false));
        final User userAfterRemoveRefreshDate = this.userRepository.findUserByPublicKeyAndHash(PUBLIC_KEY_USER_1, HASH_USER_1).get();
        assertThat(userAfterRemoveRefreshDate.getPublicKey()).isEqualTo(PUBLIC_KEY_USER_1);
        assertThat(userAfterRemoveRefreshDate.getHash()).isEqualTo(HASH_USER_1);
        assertThat(userAfterRemoveRefreshDate.getPlatform()).isEqualTo(Platform.ANDROID);
        assertThat(userAfterRemoveRefreshDate.getRefreshedAt()).isNull();
    }

    @Test
    void testRefreshUserAndResetDateForNotifications_dateShouldBeResetToNull() {
        this.userService.createUser(PUBLIC_KEY_USER_1, HASH_USER_1, CreateRequestTestUtil.createCreateUserRequest(FIREBASE_TOKEN_1));
        this.userService.createUser(PUBLIC_KEY_USER_2, HASH_USER_2, CreateRequestTestUtil.createCreateUserRequest(FIREBASE_TOKEN_2));

        this.userService.refreshUser(PUBLIC_KEY_USER_1, HASH_USER_1, Platform.ANDROID, new RefreshUserRequest(true));
        this.userService.refreshUser(PUBLIC_KEY_USER_2, HASH_USER_2, Platform.IOS, new RefreshUserRequest(true));

        final User user1BeforeReset = this.userRepository.findUserByPublicKeyAndHash(PUBLIC_KEY_USER_1, HASH_USER_1).get();
        final User user2BeforeReset = this.userRepository.findUserByPublicKeyAndHash(PUBLIC_KEY_USER_2, HASH_USER_2).get();

        assertThat(user1BeforeReset.getPublicKey()).isEqualTo(PUBLIC_KEY_USER_1);
        assertThat(user1BeforeReset.getHash()).isEqualTo(HASH_USER_1);
        assertThat(user1BeforeReset.getPlatform()).isEqualTo(Platform.ANDROID);
        assertThat(user1BeforeReset.getRefreshedAt()).isToday();

        assertThat(user2BeforeReset.getPublicKey()).isEqualTo(PUBLIC_KEY_USER_2);
        assertThat(user2BeforeReset.getHash()).isEqualTo(HASH_USER_2);
        assertThat(user2BeforeReset.getPlatform()).isEqualTo(Platform.IOS);
        assertThat(user2BeforeReset.getRefreshedAt()).isToday();

        this.userService.resetDatesForNotifiedUsers(List.of(FIREBASE_TOKEN_1, FIREBASE_TOKEN_2));

        final User user1 = this.userRepository.findUserByPublicKeyAndHash(PUBLIC_KEY_USER_1, HASH_USER_1).get();
        final User user2 = this.userRepository.findUserByPublicKeyAndHash(PUBLIC_KEY_USER_2, HASH_USER_2).get();

        assertThat(user1.getPublicKey()).isEqualTo(PUBLIC_KEY_USER_1);
        assertThat(user1.getHash()).isEqualTo(HASH_USER_1);
        assertThat(user1.getPlatform()).isEqualTo(Platform.ANDROID);
        assertThat(user1.getRefreshedAt()).isNull();

        assertThat(user2.getPublicKey()).isEqualTo(PUBLIC_KEY_USER_2);
        assertThat(user2.getHash()).isEqualTo(HASH_USER_2);
        assertThat(user2.getPlatform()).isEqualTo(Platform.IOS);
        assertThat(user2.getRefreshedAt()).isNull();
    }
}
