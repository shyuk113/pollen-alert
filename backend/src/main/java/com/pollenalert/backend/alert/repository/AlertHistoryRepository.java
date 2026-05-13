package com.pollenalert.backend.alert.repository;

import com.pollenalert.backend.alert.domain.AlertHistory;
import com.pollenalert.backend.alert.domain.AlertType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AlertHistoryRepository extends JpaRepository<AlertHistory,Long> {

    //user id로 알림 내역 내림 차순으로 조회(최신순)
    List<AlertHistory> findByUser_idOrderBySentAtDesc(Long user_id, Pageable pageable);

    //같은 알림을 보냈었는지 확인
    boolean existsByUser_idAndAlertTypeAndSentAtAfter(Long user_id, AlertType alertType, LocalDateTime sentAt);


}
