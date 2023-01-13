package com.cleevio.vexl.module.push.service;

import com.cleevio.vexl.module.push.dto.PushMessageDto;

public interface NotificationService {

    void sendPushNotification(PushMessageDto event);
}
