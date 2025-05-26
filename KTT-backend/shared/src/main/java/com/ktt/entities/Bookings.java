package com.ktt.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "bookings", schema = "ktt")
@Data
public class Bookings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String provider;

    @Column(name = "ticket_number")
    private String ticketNumber;

    private String currency;

    @Column(name = "fare_amount")
    private Double fareAmount;

    @Column(name = "booking_status")
    private String bookingStatus;

    @Column(name = "payment_type")
    private String paymentType;

    @Column(name = "pnr_no", unique = true)
    private String pnrNo;

    @Column(name = "payment_transaction_id", unique = true)
    private String paymentTransactionId;

    @Column(name = "booking_creation_date")
    private LocalDateTime bookingCreationDate = LocalDateTime.now();

    @Column(name = "creation_date")
    private LocalDateTime creationDate = LocalDateTime.now();

    @Column(name = "update_date")
    private LocalDateTime updateDate = LocalDateTime.now();

    @Column(name = "guest_id")
    private Long guestId;

    @Column(name = "service_id")
    private Integer serviceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guest_id", insertable = false, updatable = false)
    private BookingGuest guest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", insertable = false, updatable = false)
    private BookingServiceEntity bookingService;

}
