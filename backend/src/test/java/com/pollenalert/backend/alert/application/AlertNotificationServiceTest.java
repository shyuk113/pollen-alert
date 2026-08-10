package com.pollenalert.backend.alert.application;

import com.google.firebase.messaging.Message;
import com.pollenalert.backend.alert.domain.AlertSetting;
import com.pollenalert.backend.alert.domain.AlertType;
import com.pollenalert.backend.alert.infrastructure.AlertHistoryRepository;
import com.pollenalert.backend.alert.infrastructure.AlertSettingRepository;
import com.pollenalert.backend.member.domain.AllergySetting;
import com.pollenalert.backend.member.domain.Provider;
import com.pollenalert.backend.member.domain.User;
import com.pollenalert.backend.member.infrastructure.AllergySettingRepository;
import com.pollenalert.backend.pollen.domain.PollenData;
import com.pollenalert.backend.pollen.domain.Source;
import com.pollenalert.backend.pollen.infrastructure.PollenDataRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlertNotificationServiceTest {

    @Mock private AlertSettingRepository alertSettingRepository;
    @Mock private AlertHistoryRepository alertHistoryRepository;
    @Mock private AllergySettingRepository  allergySettingRepository;
    @Mock private PollenDataRepository pollenDataRepository;
    @Mock private FcmSender fcmSender;

    @InjectMocks
    private AlertNotificationService alertNotificationService;

    private User user;
    private AlertSetting setting;

    @BeforeEach
    void setUp(){
        user = User.createUserSocial("test@test.com", "테스트유저", Provider.KAKAO, "pid");
        user.updateProfile("테스트유저", "서울");
        setting = AlertSetting.createAlertSetting(user, true,1,3,"08:00", "fcm-token");
    }

    @Test
    @DisplayName("지역 미설정 유저는 처리 x")
    void RegionNotSet(){
        User user = User.createUserSocial("a@a.com","user1", Provider.KAKAO, "pid2");
        AlertSetting setting = AlertSetting.createAlertSetting(user, true,1,3,"08:00","token");

        alertNotificationService.processUserAlert(setting);

        verifyNoInteractions(allergySettingRepository, pollenDataRepository, fcmSender);
    }

    @Test
    @DisplayName("꽃가루 없는 유저에게는 알림 x")
    void NoAllergySetting() throws Exception{
        AllergySetting allergy = AllergySetting.create(user, false,"");
        when(allergySettingRepository.findByUser_id(any())).thenReturn(Optional.of(allergy));

        alertNotificationService.processUserAlert(setting);

        verifyNoInteractions(pollenDataRepository, fcmSender);
    }

    @Test
    @DisplayName("threshold 이상 데이터가 있으면 TODAY 알림을 보내고 이력 저장")
    void alertThreshold() throws Exception{
        AllergySetting allergy = AllergySetting.create(user, true,"oak");
        when(allergySettingRepository.findByUser_id(any())).thenReturn(Optional.of(allergy));

        PollenData high = PollenData.create("서울", LocalDate.now(),2,"높음", Source.KMA,"oak");
        when(pollenDataRepository.findByRegionAndForecastDateAndPollenTypeIn(eq("서울"),eq(LocalDate.now()), any())).thenReturn(List.of(high));

        when(alertHistoryRepository.existsByUser_idAndAlertTypeAndSentAtAfter(any(), eq(AlertType.TODAY), any(LocalDateTime.class))).thenReturn(false);

        alertNotificationService.processUserAlert(setting);

        verify(fcmSender, times(1)).send(any(Message.class));
        verify(alertHistoryRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("12시간 내 이미 알림을 보냈다면 중복 발송x")
    void skipAlert() throws Exception{
        AllergySetting allergy = AllergySetting.create(user, true,"oak");
        when(allergySettingRepository.findByUser_id(any())).thenReturn(Optional.of(allergy));

        PollenData high = PollenData.create("서울", LocalDate.now(), 2, "높음", Source.KMA,"oak");
        when(pollenDataRepository.findByRegionAndForecastDateAndPollenTypeIn(any(), eq(LocalDate.now()),any())).thenReturn(List.of(high));
        when(alertHistoryRepository.existsByUser_idAndAlertTypeAndSentAtAfter(any(), eq(AlertType.TODAY), any(LocalDateTime.class))).thenReturn(true);

        alertNotificationService.processUserAlert(setting);

        verify(fcmSender, never()).send(any(Message.class));
    }

    @Test
    @DisplayName("오늘 데이터는 없지만 이전 TODAY 이력이 있으면")
    void sendClearAlert() throws Exception{
        AllergySetting allergy = AllergySetting.create(user, true, "oak");
        when(allergySettingRepository.findByUser_id(any())).thenReturn(Optional.of(allergy));
        when(pollenDataRepository.findByRegionAndForecastDateAndPollenTypeIn(any(),any(),any())).thenReturn(List.of());
        when(alertHistoryRepository.existsByUser_idAndAlertTypeAndSentAtAfter(any(), eq(AlertType.TODAY) ,any(LocalDateTime.class))).thenReturn(true);

        alertNotificationService.processUserAlert(setting);

        verify(fcmSender, times(1)).send(any(Message.class));
    }
}