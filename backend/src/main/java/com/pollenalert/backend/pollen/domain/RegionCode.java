package com.pollenalert.backend.pollen.domain;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum RegionCode {
    서울특별시("1100000000"),
    부산광역시("2600000000"),
    대구광역시("2700000000"),
    인천광역시("2800000000"),
    광주광역시("2900000000"),
    대전광역시("3000000000"),
    울산광역시("3100000000"),
    세종특별자치시("3600000000"),
    경기도("4100000000"),
    강원특별자치도("4200000000"),
    충청북도("4300000000"),
    충청남도("4400000000"),
    전북특별자치도("4500000000"),
    전라남도("4600000000"),
    경상북도("4700000000"),
    경상남도("4800000000"),
    제주특별자치도("5000000000");

    private final String code;

    RegionCode(String code){
        this.code = code;
    }

    public String getCode(){
        return code;
    }

    public static List<String> getRegionNames(){
        return Arrays.stream(values()).map(Enum::name).collect(Collectors.toList());
    }

    public static String getCodeByName(String name){
        return Arrays.stream(values()).filter(r->r.name().equals(name)).map(RegionCode::getCode).findFirst().orElseThrow(()->new IllegalArgumentException("존재하지 않는 지역입니다:" + name));
    }
}
