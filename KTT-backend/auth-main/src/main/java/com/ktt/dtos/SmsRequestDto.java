package com.ktt.dtos;

import lombok.Getter;

@Getter
public class SmsRequestDto {
    private String mobileNo;
    private String message;
}
