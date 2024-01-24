package com.cleevio.vexl.module.user.service;

import com.cleevio.vexl.common.constant.ModuleLockNamespace;
import com.cleevio.vexl.common.service.AdvisoryLockService;
import com.cleevio.vexl.module.stats.constant.StatsKey;
import com.cleevio.vexl.module.stats.dto.StatsDto;
import com.cleevio.vexl.module.user.constant.Platform;
import com.cleevio.vexl.module.user.constant.UserAdvisoryLock;
import com.cleevio.vexl.module.user.dto.InactivityNotificationDto;
import com.cleevio.vexl.module.user.dto.NewContentNotificationDto;
import com.cleevio.vexl.module.user.dto.request.CreateUserRequest;
import com.cleevio.vexl.module.user.dto.request.FirebaseTokenUpdateRequest;
import com.cleevio.vexl.module.user.dto.request.RefreshUserRequest;
import com.cleevio.vexl.module.user.entity.User;
import com.cleevio.vexl.module.user.event.NewContentNotificationEvent;
import com.cleevio.vexl.module.user.event.UserInactivityLimitExceededEvent;
import com.cleevio.vexl.module.user.event.UserRemovedEvent;
import com.cleevio.vexl.module.user.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Service for creating, searching and deleting of users.
 */
@Slf4j
@Service
@Validated
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final AdvisoryLockService advisoryLockService;

    @Deprecated
    @Transactional
    public User createUser(final String publicKey, final String hash, final int clientVersion) {
        return createUser(publicKey, hash, new CreateUserRequest(null), clientVersion);
    }

    @Transactional
    public Boolean checkUserExists(final String publicKey, final String hash) {
        advisoryLockService.lock(
                ModuleLockNamespace.USER,
                UserAdvisoryLock.CREATE_USER.name(),
                publicKey
        );

        final Optional<User> userByHash = this.userRepository.findByHash(hash);
        return userByHash.isPresent();
    }


    @Transactional
    public User createUser(final String publicKey, final String hash, @Valid CreateUserRequest request, final int clientVersion) {
        advisoryLockService.lock(
                ModuleLockNamespace.USER,
                UserAdvisoryLock.CREATE_USER.name(),
                publicKey
        );

        final Optional<User> userByHash = this.userRepository.findByHash(hash);
        if (userByHash.isPresent()) {
            log.info("FacebookId or phone number is already in use by another user. Hash string: [{}]. Removing this user and create new one.",
                    hash);
            this.removeUserAndContacts(userByHash.get());
        }

        log.info("Creating an user [{}] ",
                publicKey);

        final User savedUser = this.userRepository.save(
                User.builder()
                        .publicKey(publicKey)
                        .hash(hash)
                        .firebaseToken(request.firebaseToken())
                        .clientVersion(clientVersion)
                        .refreshedAt(LocalDate.now())
                        .build()
        );

        log.info("User id - [{}] created",
                savedUser.getId());

        return savedUser;
    }

    @Transactional(readOnly = true)
    public Optional<User> findByPublicKeyAndHash(String publicKey, String hash) {
        return this.userRepository.findUserByPublicKeyAndHash(publicKey, hash);
    }

    @Transactional(readOnly = true)
    public boolean existsByPublicKeyAndHash(String publicKey, String hash) {
        return this.userRepository.existsByPublicKeyAndHash(publicKey, hash);
    }

    @Transactional
    public void removeUserAndContacts(User user) {
        log.info("Removing user with id {} and all his contacts",
                user.getId());
        this.applicationEventPublisher.publishEvent(new UserRemovedEvent(user.getPublicKey()));
        this.userRepository.delete(user);
    }

    @Transactional
    public void save(User user) {
        this.userRepository.save(user);
    }

    @Transactional
    public void updateFirebaseToken(final String publicKey, final String hash, @Valid final FirebaseTokenUpdateRequest request, int clientVersion) {
        advisoryLockService.lock(
                ModuleLockNamespace.USER,
                UserAdvisoryLock.UPDATE_USER.name(),
                publicKey
        );

        final User user = this.userRepository.findUserByPublicKeyAndHash(publicKey, hash)
                .orElseThrow(UserNotFoundException::new);
        user.setFirebaseToken(request.firebaseToken());
        user.setClientVersion(clientVersion);
    }

    @Transactional
    public void deleteUnregisteredToken(final String firebaseToken) {
        this.userRepository.findByFirebaseTokens(List.of(firebaseToken))
                .forEach(it -> it.setFirebaseToken(null));
    }

    @Transactional(readOnly = true)
    public List<StatsDto> retrieveStats(final StatsKey... statsKeys) {
        final List<StatsDto> statsDtos = new ArrayList<>();
        Arrays.stream(statsKeys).forEach(statKey -> {
            switch (statKey) {
                case ALL_TIME_USERS_COUNT -> statsDtos.add(new StatsDto(
                        StatsKey.ALL_TIME_USERS_COUNT,
                        this.userRepository.getAllTimeUsersCount()
                ));
                case ACTIVE_USERS_COUNT -> statsDtos.add(new StatsDto(
                        StatsKey.ACTIVE_USERS_COUNT,
                        this.userRepository.getActiveUsersCount()
                ));
                case REMOVED_USERS_COUNT -> statsDtos.add(new StatsDto(
                        StatsKey.REMOVED_USERS_COUNT,
                        this.userRepository.getAllTimeUsersCount() - this.userRepository.getActiveUsersCount()
                ));
            }
        });
        return statsDtos;
    }

    @Transactional
    public void refreshUser(final String publicKey,
                            final String hash,
                            final Platform platform,
                            @Valid final RefreshUserRequest request,
                            final int clientVersion) {
        User user = this.userRepository.findUserByPublicKeyAndHash(publicKey, hash)
                .orElseThrow(UserNotFoundException::new);
        user.setRefreshedAt(request.offersAlive() ? LocalDate.now() : null);
        user.setClientVersion(clientVersion);
        user.setPlatform(platform);
    }

    @Transactional(readOnly = true)
    public void processNotificationsForInactivity(@NotNull @PositiveOrZero final Integer notificationAfter) {
        if (!advisoryLockService.tryLock(
                ModuleLockNamespace.USER,
                UserAdvisoryLock.INACTIVITY_TASK.name()
        )) {
            log.info("Could not obtain lock for user inactivity task.");
            return;
        }

        final List<InactivityNotificationDto> inactivityNotificationDtos = this.userRepository.retrieveFirebaseTokensOfInactiveUsers(LocalDate.now().minusDays(notificationAfter));
        if (inactivityNotificationDtos.isEmpty()) {
            return;
        }

        applicationEventPublisher.publishEvent(new UserInactivityLimitExceededEvent(inactivityNotificationDtos));
    }

    @Transactional()
    public void notifyInactiveUsersAboutNewContent(@NotNull @PositiveOrZero final Integer notificationAfterDays) {
        final List<User> users = this.userRepository.retrieveFirebaseTokensForNewContentNotification(LocalDate.now().minusDays(notificationAfterDays));
        if (users.isEmpty()) {
            return;
        }

        users.forEach(it -> it.setLastNewContentNotificationSentAt(LocalDate.now()));
        this.userRepository.saveAll(users);

        final var notificationDtos = users
                .stream()
                .filter(it -> it.getFirebaseToken() != null)
                .map(it -> new NewContentNotificationDto(
                        it.getFirebaseToken(),
                        it.getPlatform(),
                        it.getClientVersion()
                ))
                .toList();

        applicationEventPublisher.publishEvent(new NewContentNotificationEvent(notificationDtos));
    }

    @Transactional
    public void resetDatesForNotifiedUsers(List<String> firebaseTokens) {
        this.userRepository.findByFirebaseTokens(firebaseTokens)
                .forEach(it -> it.setRefreshedAt(null));
    }
}
