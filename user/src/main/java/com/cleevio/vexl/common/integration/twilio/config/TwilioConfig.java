package com.cleevio.vexl.common.integration.twilio.config;

import com.twilio.Twilio;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class TwilioConfig {

    @Getter
    private final String phone;

    @Getter
    private final String verifyServiceSid;

    public TwilioConfig(@Value("${twilio.sid}") String sid,
                        @Value("${twilio.token}") String token,
                        @Value("${twilio.phone}") String phone,
                        @Value("${twilio.verify-service-sid}") String verifyServiceSid) {
        this.phone = phone;
        this.verifyServiceSid = verifyServiceSid;

        if (!sid.isEmpty() && !token.isEmpty() && !phone.isEmpty() && !verifyServiceSid.isEmpty()) {
            Twilio.init(sid, token);

            log.info("Twilio initialized");
        } else {
            log.error("Twilio cannot be initialized");
        }
    }
}
