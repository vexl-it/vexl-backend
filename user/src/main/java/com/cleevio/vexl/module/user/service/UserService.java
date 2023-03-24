package com.cleevio.vexl.module.user.service;

import com.cleevio.vexl.common.constant.ModuleLockNamespace;
import com.cleevio.vexl.common.service.AdvisoryLockService;
import com.cleevio.vexl.module.user.dto.UserData;
import com.cleevio.vexl.module.user.dto.request.ChallengeRequest;
import com.cleevio.vexl.module.user.entity.User;
import com.cleevio.vexl.module.user.constant.UserAdvisoryLock;
import com.cleevio.vexl.module.user.event.UserRemovedEvent;
import com.cleevio.vexl.module.user.exception.UserAlreadyExistsException;
import com.cleevio.vexl.module.user.exception.UserNotFoundException;
import com.cleevio.vexl.module.user.exception.VerificationExpiredException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import java.util.Optional;

/**
 * Service for processing of user - creating, updating, searching, deleting.
 * <p>
 * Life cycle of user:
 * 1. user's phone number is verified, so we prepare user - that means we create user entity with public key
 * 2. user's challenge is verified, so user can create his account - that means we add values as username and avatar to his account
 * 3. user log out of application, we delete user
 */
@Service
@Validated
@Slf4j
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final AdvisoryLockService advisoryLockService;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional
    public User prepareUser(String publicKey) {
        advisoryLockService.lock(
                ModuleLockNamespace.USER,
                UserAdvisoryLock.PREPARE_USER.name(),
                publicKey
        );

        if (this.userRepository.existsUserByPublicKey(publicKey)) {
            throw new UserAlreadyExistsException();
        }

        final User user = User.builder()
                .publicKey(publicKey)
                .build();

        final User savedUser = this.userRepository.save(user);
        log.info("User with ID {} is prepared.", user.getId());

        return savedUser;
    }

    @Transactional
    public void remove(User user) {
        advisoryLockService.lock(
                ModuleLockNamespace.USER,
                UserAdvisoryLock.MODIFYING_USER.name(),
                user.getPublicKey()
        );

        this.userRepository.delete(user);
        applicationEventPublisher.publishEvent(new UserRemovedEvent(user));
    }

    @Transactional(readOnly = true)
    public Optional<User> findByPublicKey(String publicKey) {
        return this.userRepository.findByPublicKey(publicKey);
    }

    @Transactional
    public User save(User user) {
        return this.userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public UserData findValidUserWithChallenge(@Valid ChallengeRequest request) {
        final User user = findByPublicKey(request.userPublicKey())
                .orElseThrow(UserNotFoundException::new);

        if (user.getUserVerification() == null || user.getUserVerification().getChallenge() == null) {
            throw new VerificationExpiredException();
        }

        return new UserData(
                user.getPublicKey(),
                user.getUserVerification().getPhoneNumber(),
                user.getUserVerification().getChallenge(),
                request.signature()
        );
    }

    @Transactional(readOnly = true)
    public long getUsersCount() {
        return userRepository.count();
    }


    public void willFail() {
        userRepository.willFail();
    }
}
