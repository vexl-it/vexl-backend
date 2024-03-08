package com.cleevio.vexl.module.push.service;

import com.cleevio.vexl.module.inbox.event.NewMessageReceivedEvent;
import com.cleevio.vexl.module.push.dto.PushMessageDto;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;


@Service
@Validated
@RequiredArgsConstructor
public class PushService {

    private final NotificationService notificationService;

    public void sendPushNotification(@Valid NewMessageReceivedEvent event) {
        notificationService.sendPushNotification(createPushMessageDto(event));
    }

    private PushMessageDto createPushMessageDto(
            NewMessageReceivedEvent event
    ) {
        return new PushMessageDto(
                event.token(),
                event.platform(),
                event.clientVersion(),
                event.messageType(),
                event.receiverPublicKey(),
                event.senderPublicKey(),
                event.messagePreview()
        );
    }
}
