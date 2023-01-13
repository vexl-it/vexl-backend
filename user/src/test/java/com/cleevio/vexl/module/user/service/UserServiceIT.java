package com.cleevio.vexl.module.user.service;

import com.cleevio.vexl.common.IntegrationTest;
import com.cleevio.vexl.module.user.entity.User;
import com.cleevio.vexl.module.user.exception.UserAlreadyExistsException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@IntegrationTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserServiceIT {

    private static final String USER_PUBLIC_KEY_1 = "dummy_user_public_key_1";
    private final UserService userService;
    private final UserRepository userRepository;

    @Autowired
    public UserServiceIT(UserService userService, UserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
    }

    @Test
    void testPrepareDuplicatedUser_shouldReturnException() {
        userService.prepareUser(USER_PUBLIC_KEY_1);

        assertThrows(
                UserAlreadyExistsException.class,
                () -> userService.prepareUser(USER_PUBLIC_KEY_1)
        );
    }

    @Test
    void testPrepareUser_shouldUpdateUser() {
        final User prepareUser = userService.prepareUser(USER_PUBLIC_KEY_1);
        final Long userId = prepareUser.getId();
        final User savedPreparedUser = userRepository.findById(userId).get();

        assertThat(savedPreparedUser.getPublicKey()).isEqualTo(USER_PUBLIC_KEY_1);
    }
}
