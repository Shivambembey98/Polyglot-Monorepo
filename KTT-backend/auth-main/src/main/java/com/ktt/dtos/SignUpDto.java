package com.ktt.dtos;

import com.ktt.enums.UserRole;

public record SignUpDto(
    String login,
    String password,
    String emailId,
    String title,
    String firstName,
    String middleName,
    String lastName,
    String address,
    String identification,
    byte[] Photo,
    String mobileNo,
    String companyCode,
    UserRole role) {
}
