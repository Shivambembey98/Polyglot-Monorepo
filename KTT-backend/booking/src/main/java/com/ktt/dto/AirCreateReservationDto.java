package com.ktt.dto;

import lombok.Data;

import java.util.List;
@Data
public class AirCreateReservationDto {

     List<TravelDto> travelDtoList;
    private String street;
    private String city;
    private String state;
    private String postalCode;
    private String country;
    private String sessionIdAirPrice;
    private String sessionIdLfs;
    private String airLine;

}
