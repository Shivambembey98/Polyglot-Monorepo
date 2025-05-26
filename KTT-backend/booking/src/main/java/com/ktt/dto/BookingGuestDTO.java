// BookingGuestDTO.java
package com.ktt.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class BookingGuestDTO {
    private String firstName;
    private String lastName;
    private String title;
    private String gender;
    private String travelerType;
    private String phoneNumber;
    private String emailAddress;
    private LocalDate dob;
    private String address;
    private String identificationId;
    private Integer bookingSerId; // Foreign key
}