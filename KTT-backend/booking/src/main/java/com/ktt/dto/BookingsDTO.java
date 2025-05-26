// BookingsDTO.java
package com.ktt.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class BookingsDTO {
    private String provider;
    private String ticketNumber;
    private String currency;
    private Double fareAmount;
    private String bookingStatus;
    private String paymentType;
    private String pnrNo;
    private String paymentTransactionId;
    private Long guestId; // Foreign key
    private Integer serviceId; // Foreign key
}