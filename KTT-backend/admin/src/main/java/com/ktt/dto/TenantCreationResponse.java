package com.ktt.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TenantCreationResponse {
    private String email;
    private String username;
    private String status;
}