package com.pollenalert.backend.alert.repository;

import com.pollenalert.backend.alert.domain.AlertSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AlertSettingRepository extends JpaRepository<AlertSetting,Long> {

    //user id로 설정 조회
    Optional<AlertSetting> findByUser_id(Long user_id);

    //알림 활성화된 모든 설정 조회
    List<AlertSetting> findAllByEnabledTrue();
}
