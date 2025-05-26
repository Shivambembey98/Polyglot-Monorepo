package com.ktt.entities;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@EqualsAndHashCode(of = "id")
@Table(name = "airport_list",schema ="ktt")
public class AirportList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // Assuming id is auto-incremented
    private Long id;

    @Column(name = "airport_name")
    private String airportName;

    @Column(name = "airport_city")
    private String airportCity;

    @Column(name = "iata_code")
    private String iataCode;
    @Column(name = "country")
    private String country;
    @Column(name = "country_name")
    private String countryName;
}
