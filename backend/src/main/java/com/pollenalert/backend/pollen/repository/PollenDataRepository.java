package com.pollenalert.backend.pollen.repository;

import com.pollenalert.backend.pollen.domain.PollenData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PollenDataRepository extends JpaRepository<PollenData,Long> {

    //지역, 날짜를 통해 조회
    Optional<PollenData> findByRegionAndForecastDate(String region, LocalDate forecastDate);

    //지역, 날짜 범위로 조회
    List<PollenData> findByRegionAndForecastDateBetweenOrderByForecastDateAsc(String region, LocalDate startDate, LocalDate endDate);
}
