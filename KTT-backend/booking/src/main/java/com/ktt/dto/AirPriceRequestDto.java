package com.ktt.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Data
@Getter
@Setter
public class AirPriceRequestDto {
    private List<FlightDto> flights;
    private int students;
    private int seniors;
    private int adults;
    private int children;
    private int Infants;
    private String sessionId;
}
