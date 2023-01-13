package com.cleevio.vexl.module.push.event.listener;

import com.cleevio.vexl.module.inbox.event.NewMessageReceivedEvent;
import com.cleevio.vexl.module.push.service.PushService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class InboxPushEventListener {

    private final PushService pushService;

    @EventListener
    public void onPushNotificationEvent(final NewMessageReceivedEvent event) {
        this.pushService.sendPushNotification(event);
    }
}
