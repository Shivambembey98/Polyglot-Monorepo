package com.ktt.dto;


import com.ktt.repository.AppConstants;
import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TenantRequestDTO {

    @NotBlank(message = AppConstants.Validation.TENANT_NAME_REQUIRED)
    private String tenantName;

    @NotBlank(message = AppConstants.Validation.USERNAME_REQUIRED)
    @Size(min = 4, max = 50, message = AppConstants.Validation.USERNAME_LENGTH)
    private String username;


    @Size(min = 6, message = AppConstants.Validation.PASSWORD_LENGTH)
    private String userPassword;

    @NotBlank(message = AppConstants.Validation.EMAIL_REQUIRED)
    @Email(message = AppConstants.Validation.EMAIL_INVALID)
    private String email;

    @Pattern(regexp = "^[0-9]{10}$", message = AppConstants.Validation.MOBILE_INVALID)
    private String mobile;

    private String companyCode;
    private String companyName;
    private String createdBy;
}