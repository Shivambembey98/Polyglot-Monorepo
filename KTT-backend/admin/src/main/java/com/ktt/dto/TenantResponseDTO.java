package com.ktt.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TenantResponseDTO {
    private Long id;
    private String tenantName;
    private String username;
    private String email;
    private String mobile;
    private String companyCode;
    private String companyName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String updatedBy;
    private Boolean isActive;
    private Boolean isDeleted;
}