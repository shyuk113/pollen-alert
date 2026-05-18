package com.pollenalert.backend.pollen.service;

import com.pollenalert.backend.pollen.domain.PollenData;
import com.pollenalert.backend.pollen.domain.RegionCode;
import com.pollenalert.backend.pollen.domain.Source;
import com.pollenalert.backend.pollen.dto.KmaPollenResponseDto;
import com.pollenalert.backend.pollen.repository.PollenDataRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PollenCollector {

    private final WebClient webClient;
    private final PollenDataRepository  pollenDataRepository;

    @Value("${kma.api-key}")
    private String apiKey;

    @Value("${kma.base-url}")
    private String baseUrl;

    //꽃가루 엔드포인트
    private static final List<String> ENDPOINTS = Arrays.asList(
            "/getOakPollenRiskIdxV3",    // 참나무
            "/getPinePollenRiskIdxV3",   // 소나무
            "/getWeedsPollenRiskndxV3"   // 잡초류
    );

    private static final Map<String, String> ENDPOINT_TYPE_MAP = Map.of(
            "/getOakPollenRiskIdxV3", "oak",
            "/getPinePollenRiskIdxV3", "pine",
            "/getWeedsPollenRiskndxV3", "weed"
    );

    //6시간 마다 수집
    @Scheduled(cron = "0 0 0/6 * * *")//
    public void collect(){
        log.info("꽃가루 데이터 수집 시작");
        String time = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyMMdd")) + "18";

        for (RegionCode region : RegionCode.values()){
            for (String endpoint : ENDPOINTS){
                String pollenType = ENDPOINT_TYPE_MAP.get(endpoint);
                try{
                    collectPollenData(region,endpoint,time,pollenType);
                } catch (Exception e){
                    log.error("꽃가루 데이터 수집 실패: region={}, endpoint={}, error={}", region.name(),endpoint,e.getMessage());
                }
            }
        }
        log.info("꽃가루 데이터 수집 완료");
    }

    private void collectPollenData(RegionCode region, String endpoint, String time, String pollenType){
        KmaPollenResponseDto response = webClient.get().uri(baseUrl + endpoint + "?serviceKey={key}&numOfRows=10&pageNo=1&dataType=JSON&areaNo={areaNo}&time={time}", apiKey, region.getCode(),time).retrieve().bodyToMono(KmaPollenResponseDto.class).block();

        if(response == null || response.getResponse() == null || response.getResponse().getBody() == null || response.getResponse().getBody().getItems() == null || response.getResponse().getBody().getItems().getItem() == null){
            return;
        }

        KmaPollenResponseDto.Item item = response.getResponse().getBody().getItems().getItem().get(0);
        savePollenData(region.name(),item,pollenType);
    }

    //4일치 저장
    private void savePollenData(String regionName, KmaPollenResponseDto.Item item, String pollenType){
        saveSingleDay(regionName, LocalDate.now(), item.getToday(), pollenType);
        saveSingleDay(regionName, LocalDate.now().plusDays(1), item.getTomorrow(), pollenType);
        saveSingleDay(regionName, LocalDate.now().plusDays(2), item.getDayaftertomorrow(), pollenType);
        saveSingleDay(regionName, LocalDate.now().plusDays(3), item.getTodaysaftertomorrow(), pollenType);
    }

    private void saveSingleDay(String regionName, LocalDate date, String levelStr, String pollenType){
        if (levelStr == null|| levelStr.isBlank()) return;

        int level = Integer.parseInt(levelStr);
        String grade = levelToGrade(level);

        if(pollenDataRepository.findByRegionAndForecastDateAndPollenType(regionName, date, pollenType).isPresent()){
            return;
        }

        PollenData data = PollenData.create(regionName, date, level, grade, Source.KMA, pollenType);

        pollenDataRepository.save(data);
    }

    private String levelToGrade(int level){
        return switch (level) {
            case 0 -> "낮음";
            case 1 -> "보통";
            case 2 -> "높음";
            case 3 -> "매우 높음";
            default -> "알수없음";
        };
    }

    @PostConstruct
    public void init() {
        collect();
    }
}
