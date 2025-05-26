package com.ktt.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ApiResponse {
    private String success;
    private String message; // For additional information if needed
    private Object data;   //Data returned from the API
}
