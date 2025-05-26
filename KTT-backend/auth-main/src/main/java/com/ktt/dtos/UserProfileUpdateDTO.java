package com.ktt.dtos;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;



@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileUpdateDTO {

    private String firstName;


    private String middleName;


    private String lastName;


    private String mobileNo;


    private String address;


    private String city;


    private String state;


    private String nationality;


    private String pinCode;


    private String gender;

    private String dob;
}