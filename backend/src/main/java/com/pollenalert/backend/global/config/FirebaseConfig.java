package com.pollenalert.backend.global.config;


import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;

@Configuration
public class FirebaseConfig {

    @Bean
    public FirebaseMessaging firebaseMessaging(){
        return FirebaseMessaging.getInstance();
    }

    @PostConstruct
    public void initialize() throws IOException {
        InputStream serviceAccount = getClass().getResourceAsStream("/firebase-service-account.json");

        if (serviceAccount == null) {
            throw new IllegalStateException("Firebase 서비스 계정 파일을 찾을 수 없습니다: /firebase-service-account.json");
        }

        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount)).build();

        if (FirebaseApp.getApps().isEmpty()){
            FirebaseApp.initializeApp(options);
        }
    }

}
