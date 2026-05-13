package com.pollenalert.backend.alert.domain;

public enum AlertType {
    D_3,    //3일전 경보
    D_1,    //하루 전 경보
    TODAY,  //당일 고농도 경보
    CLEAR;  //해소 알림
}
