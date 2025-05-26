package com.ktt.entities;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "travelers" ,schema = "ktt")
public class Traveler {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(nullable = false)
    private String prefix;

    @Column(nullable = false)
    private String gender;

    @Column(name = "traveler_type", nullable = false)
    private String travelerType;

    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    @Column(nullable = false)
    private String email;

    @Column(name = "doc_text")
    private String docText;

    @Column(nullable = false)
    private String dob;
}