package com.cleevio.vexl.module.push.service;

import com.cleevio.vexl.common.constant.ModuleLockNamespace;
import com.cleevio.vexl.common.integration.firebase.service.NotificationService;
import com.cleevio.vexl.common.service.AdvisoryLockService;
import com.cleevio.vexl.module.push.constant.NotificationType;
import com.cleevio.vexl.module.push.constant.PushAdvisoryLock;
import com.cleevio.vexl.module.push.dto.NotificationDto;
import com.cleevio.vexl.module.push.dto.PushNotification;
import com.cleevio.vexl.module.push.entity.Push;
import com.cleevio.vexl.module.user.dto.InactivityNotificationDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@Validated
@RequiredArgsConstructor
public class PushService {
    private final NotificationService notificationService;
    private final PushRepository pushRepository;
    private final AdvisoryLockService advisoryLockService;

    public void sendImportedNotification(Set<String> firebaseTokens, Set<String> secondDegreeFirebaseTokens, String newUserPublicKey) {
        if (firebaseTokens.isEmpty()) {
            return;
        }
        this.notificationService.sendPushNotification(new PushNotification(NotificationType.NEW_APP_USER, null, newUserPublicKey, firebaseTokens, secondDegreeFirebaseTokens));
    }

    @Transactional
    public void saveNotification(@Valid final NotificationDto notification) {
        this.pushRepository.save(new Push(notification.groupUuid(), notification.membersFirebaseTokens()));
    }

    @Transactional
    public Map<String, Set<String>> processPushNotification() {
        advisoryLockService.lock(
                ModuleLockNamespace.GROUP,
                PushAdvisoryLock.PUSH_TASK.name()
        );
        this.pushRepository.deleteOrphans();

        final List<Push> pushes = pushRepository.findAllPushNotificationsWithExistingGroup();
        if (pushes.isEmpty()) {
            return Map.of();
        }

        final Map<String, Set<String>> notifications = new HashMap<>();
        pushes.forEach(push -> {
            Set<String> tokens = new HashSet<>();
            tokens.addAll(notifications.getOrDefault(push.getGroupUuid(), new HashSet<>()));
            tokens.addAll(push.getFirebaseTokens());
            notifications.put(push.getGroupUuid(), tokens);
        });

        sendNewGroupMemberNotification(notifications);
        this.pushRepository.deleteAllInBatch(pushes);
        return notifications;
    }

    private void sendNewGroupMemberNotification(Map<String, Set<String>> notifications) {
        notifications.forEach((k, v) -> this.notificationService.sendPushNotification(new PushNotification(NotificationType.GROUP_NEW_MEMBER, k, null, v, Collections.emptySet())));
    }

    public void sendInactivityReminderNotification(List<InactivityNotificationDto> inactivityNotificationDto) {
        notificationService.sendInactivityReminderNotification(inactivityNotificationDto);
    }
}
