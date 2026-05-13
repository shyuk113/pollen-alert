package com.pollenalert.backend.member.repository;

import com.pollenalert.backend.member.domain.AllergySetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AllergySettingRepository extends JpaRepository<AllergySetting,Long> {
    //user_id로 설정 조회
    Optional<AllergySetting> findByUser_id(Long user_id);
}
