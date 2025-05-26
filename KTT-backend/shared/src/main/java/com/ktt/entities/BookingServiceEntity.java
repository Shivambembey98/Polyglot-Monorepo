package com.ktt.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "booking_services", schema = "ktt")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class BookingServiceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "booking_service_id")
    private Integer bookingServiceId;

    @Column(name = "from_origin", nullable = false)
    private String fromOrigin;

    @Column(name = "destination", nullable = false)
    private String destination;

    @Column(name = "trip_type")
    private String tripType;

    @Column(name = "start_datetime", nullable = false)
    private LocalDateTime startDatetime;

    @Column(name = "end_datetime", nullable = false)
    private LocalDateTime endDatetime;

    @Column(name = "luggage_weight")
    private String luggageWeight;

    @Column(name = "trip_fare_amount")
    private Double tripFareAmount;

    @Column(name = "creation_date")
    private LocalDateTime creationDate = LocalDateTime.now();

    @Column(name = "update_date")
    private LocalDateTime updateDate = LocalDateTime.now();

    @OneToMany(mappedBy = "bookingService", cascade = CascadeType.ALL)
    private List<BookingGuest> guests;

    @OneToMany(mappedBy = "bookingService", cascade = CascadeType.ALL)
    private List<Bookings> bookings;


}
