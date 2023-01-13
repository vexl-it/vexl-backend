package com.cleevio.vexl.module.push.event.listener;

import com.cleevio.vexl.module.contact.event.GroupJoinedEvent;
import com.cleevio.vexl.module.push.dto.NotificationDto;
import com.cleevio.vexl.module.push.service.PushService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;

@Component
@Validated
@RequiredArgsConstructor
class GroupPushEventListener {

    private final PushService pushService;

    @EventListener
    public void onGroupJoinedEvent(@Valid final GroupJoinedEvent event) {
        final NotificationDto notificationDto = new NotificationDto(
                event.groupUuid(),
                event.membersFirebaseTokens()
        );
        pushService.saveNotification(notificationDto);
    }

}