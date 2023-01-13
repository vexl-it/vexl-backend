package com.cleevio.vexl.common.integration.firebase.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.io.IOException;

@Slf4j
@Configuration
public class FirebaseConfig {

    public FirebaseConfig(FirebaseProperties properties) {
        try {
            JSONObject json = new JSONObject();
            json.put("client_id", properties.clientId());
            json.put("client_email", properties.clientEmail());
            json.put("private_key", properties.privateKey());
            json.put("private_key_id", properties.privateKeyId());
            json.put("project_id", properties.projectId());
            json.put("token_uri", properties.tokenUri());
            json.put("type", properties.serviceType());

            final var inputStream = new ByteArrayInputStream(json.toString().getBytes());

            var options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(inputStream)).build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                log.info("Firebase application has been initialized");
            }

        } catch (IOException e) {
            log.error("Could not initialize Firebase application", e);
        }
    }
}
