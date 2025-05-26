package com.ktt.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class FlightDto {
    private String flightKey;
    private String origin;
    private String destination;
    private String departureTime;
    private String arrivalTime;
    private String carrier;
    private String flightNumber;
    private String fairBasisCode;
    private String providerCode;
    private String group;
}