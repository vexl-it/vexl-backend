package com.cleevio.vexl.common.integration.firebase.service;

import com.cleevio.vexl.common.integration.firebase.event.FirebaseTokenInvalidatedEvent;
import com.cleevio.vexl.module.inbox.constant.Platform;
import com.cleevio.vexl.module.push.dto.PushMessageDto;
import com.cleevio.vexl.module.push.service.NotificationService;
import com.google.firebase.messaging.*;
import it.vexl.common.constants.ClientVersion;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
    private static final String PREVIEW = "preview";

    @Value("${settings.ios_app_bundle_id}")
    private final String iosAppBundleId;

    public void sendPushNotification(final PushMessageDto dto) {
        if (Platform.CLI.equals(dto.platform())) {
            log.info("Can not send push notification to CLI platform");
            return;
        }

        try {
            var messageBuilder = Message.builder();

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
                    .putHeader("apns-priority", "10")
                    .putHeader("apns-push-type", "alert")
                    .putHeader("apns-topic", iosAppBundleId)

                    .build();
            messageBuilder.setApnsConfig(apnsConfig);

            messageBuilder.setToken(dto.token());
            messageBuilder.putData(TYPE, dto.messageType());
            messageBuilder.putData(INBOX, dto.receiverPublicKey());
            messageBuilder.putData(SENDER, dto.senderPublicKey());
            if(dto.messagePreview() != null)
                messageBuilder.putData(PREVIEW, dto.messagePreview());

            final String response = FirebaseMessaging.getInstance().send(messageBuilder.build());
            log.info("Sent message: " + response);
        } catch (FirebaseMessagingException e) {
            handleException(e, dto);
        } catch (Exception e) {
            log.error("Error sending notification: " + e.getMessage(), e);
        }
    }

    private void handleException(final FirebaseMessagingException e, PushMessageDto dto) {
        switch (e.getErrorCode()) {
            case NOT_FOUND -> applicationEventPublisher.publishEvent(new FirebaseTokenInvalidatedEvent(dto.senderPublicKey(), dto.token()));
            default -> log.error("Error errored during sending push notification: " + e.getMessage(), e);
        }
    }
}
