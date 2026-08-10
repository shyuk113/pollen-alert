package com.pollenalert.backend.alert.application;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MessagingErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class FcmSender {

    private final FirebaseMessaging firebaseMessaging;

    @Retryable(includes = TransientFcmException.class, maxRetries = 4, multiplier = 2)
    public void send(Message message){
        try{
            firebaseMessaging.send(message);
        } catch(FirebaseMessagingException e){
            MessagingErrorCode code = e.getMessagingErrorCode();
            if(code == MessagingErrorCode.UNREGISTERED || code == MessagingErrorCode.INVALID_ARGUMENT){
                throw new FcmTokenInvalidException(e);
            }
            log.warn("FCM발송 실패 재시도 예정: {}",e.getMessage());
            throw new TransientFcmException(e);
        }
    }
}
