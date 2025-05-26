package com.ktt.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "booking_guest", schema = "ktt")
@Data
public class BookingGuest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "booking_ser_id", nullable = false)
    private Integer bookingSerId;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "title")
    private String title;

    @Column(name = "gender")
    private String gender;

    @Column(name = "traveler_type")
    private String travelerType;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "email_address", unique = true)
    private String emailAddress;

    @Column(name = "dob")
    private LocalDate dob;

    @Column(name = "address")
    private String address;

    @Column(name = "identification_id")
    private String identificationId;

    @Column(name = "creation_date")
    private LocalDateTime creationDate = LocalDateTime.now();

    @Column(name = "update_date")
    private LocalDateTime updateDate = LocalDateTime.now();

    @Column(name = "user_logged_in")
    private Boolean userLoggedIn = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_ser_id", insertable = false, updatable = false)
    private BookingServiceEntity bookingService;

}
