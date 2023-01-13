package com.cleevio.vexl.module.user.dto;

import com.cleevio.vexl.module.user.constant.Platform;

public interface InactivityNotificationDto {

    default String getTitle() {
        return "Už jsme o tobě dlouho neslyšeli Vexláku!";
    }

    default String getBody() {
        return "Pojď si zavexlit! Jinak tvoje nabídky budou brzy smazány pro tvoji neaktivitu.";
    }

    String getFirebaseToken();

    Platform getPlatform();

}
