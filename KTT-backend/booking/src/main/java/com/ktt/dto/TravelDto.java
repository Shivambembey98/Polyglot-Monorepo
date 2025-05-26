package com.ktt.dto;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class TravelDto {
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String email;
    private String prefix;
    private String gender;
    private String travelerType;
    private String docText;
    private String dob;
    private boolean wheelchairNeeded;

}
