package com.ktt.dtos;

public record SignInDto(
    String login,
    String password,
    String companyCode) {
}
