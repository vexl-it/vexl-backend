package com.cleevio.vexl.module.user.dto;

import com.cleevio.vexl.module.user.constant.Platform;

public interface InactivityNotificationDto {

    default String getTitle() {
        return "Už jsme o tobě dlouho neslyšeli!";
    }

    default String getBody() {
        return "Máš nabídky? Otevři appku, jinak budou kvůli neaktivitě deaktivovány.";
    }

    String getFirebaseToken();

    Platform getPlatform();

}
