// BookingServiceDTO.java
package com.ktt.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class BookingServiceDTO {
    private String fromOrigin;
    private String destination;
    private String tripType;
    private LocalDateTime startDatetime;
    private LocalDateTime endDatetime;
    private String luggageWeight;
    private Double tripFareAmount;
}