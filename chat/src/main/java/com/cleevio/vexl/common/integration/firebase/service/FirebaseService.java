package com.cleevio.vexl.common.integration.firebase.service;

import com.cleevio.vexl.common.integration.firebase.event.FirebaseTokenInvalidatedEvent;
import com.cleevio.vexl.module.inbox.constant.Platform;
import com.cleevio.vexl.module.push.dto.PushMessageDto;
import com.cleevio.vexl.module.push.service.NotificationService;
import com.google.firebase.messaging.*;
import it.vexl.common.constants.ClientVersion;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class FirebaseService implements NotificationService {

    private final ApplicationEventPublisher applicationEventPublisher;
    private static final String TITLE = "title";
    private static final String BODY = "body";
    private static final String TYPE = "type";
    private static final String INBOX = "inbox";
    private static final String SENDER = "sender";

    public void sendPushNotification(final PushMessageDto dto) {
        if (Platform.CLI.equals(dto.platform())) {
            log.info("Can not send push notification to CLI platform");
            return;
        }

        final boolean sendSystemNotification = dto.clientVersion() < ClientVersion.DO_NOT_SENT_SYSTEM_NOTIFICATION_FROM_THIS_VERSION_ON && dto.title() != null && dto.text() != null;
        try {
            var messageBuilder = Message.builder();


            if(sendSystemNotification) {
                if (Platform.IOS.equals(dto.platform())) {
                    messageBuilder.setNotification(Notification.builder().setTitle(dto.title()).setBody(dto.text()).build());
                }

                messageBuilder.putData(TITLE, dto.title());
                messageBuilder.putData(BODY, dto.text());
            }

            if (Platform.ANDROID.equals(dto.platform())) {
                messageBuilder.setAndroidConfig(
                        AndroidConfig.builder()
                                .setPriority(AndroidConfig.Priority.HIGH)
                                .build()
                );
            }

            final ApnsConfig apnsConfig = ApnsConfig.builder()
                    .setAps(Aps.builder()
                            .setContentAvailable(true)
                            .build())
                    .build();
            messageBuilder.setApnsConfig(apnsConfig);

            messageBuilder.setToken(dto.token());
            messageBuilder.putData(TYPE, dto.messageType().name());
            messageBuilder.putData(INBOX, dto.receiverPublicKey());
            messageBuilder.putData(SENDER, dto.senderPublicKey());

            final String response = FirebaseMessaging.getInstance().send(messageBuilder.build());
            log.info("Sent message: " + response);
        } catch (FirebaseMessagingException e) {
            handleException(e, dto);
        } catch (Exception e) {
            log.error("Error sending notification: " + e.getMessage(), e);
        }
    }

    private void handleException(final FirebaseMessagingException e, PushMessageDto dto) {
        switch (e.getErrorCode().name()) {
            case ErrorCode.TOKEN_NOT_REGISTERED, ErrorCode.INVALID_TOKEN -> applicationEventPublisher.publishEvent(new FirebaseTokenInvalidatedEvent(dto.senderPublicKey(), dto.token()));
            default -> log.error("Error errored during sending push notification: " + e.getMessage(), e);
        }
    }

    private static class ErrorCode {
        public final static String INVALID_TOKEN = "messaging/invalid-registration-token";
        public final static String TOKEN_NOT_REGISTERED = "messaging/registration-token-not-registered";
    }
}
