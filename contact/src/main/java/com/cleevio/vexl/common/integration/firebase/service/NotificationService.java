package com.cleevio.vexl.common.integration.firebase.service;

import com.cleevio.vexl.module.push.dto.PushNotification;
import com.cleevio.vexl.module.user.dto.InactivityNotificationDto;

import java.util.List;

public interface NotificationService {

    void sendPushNotification(PushNotification groupJoined);

    void sendInactivityReminderNotification(List<InactivityNotificationDto> firebaseTokens);
}
