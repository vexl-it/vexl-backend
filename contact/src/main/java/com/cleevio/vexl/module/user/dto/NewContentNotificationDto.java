package com.cleevio.vexl.module.user.dto;

import com.cleevio.vexl.module.user.constant.Platform;

public record NewContentNotificationDto(
        String firebaseToken,
        Platform platform,
        int clientVersion
){}
