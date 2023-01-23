package com.cleevio.vexl.module.user.dto;

import com.cleevio.vexl.module.user.constant.Platform;

public interface InactivityNotificationDto {

    default String getTitle() {
        return "Tvoje nabídka bude brzy deaktivována.";
    }

    default String getBody() {
        return "Už jsme o tobě dlouho neslyšeli! Otevři appku, jinak budou tvoje nabídky kvůli neaktivitě deaktivovány.";
    }

    String getFirebaseToken();

    Platform getPlatform();

}
