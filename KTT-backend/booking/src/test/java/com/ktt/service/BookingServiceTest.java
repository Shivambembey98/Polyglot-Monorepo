package com.ktt.service;

import com.ktt.entities.AirportList;
import com.ktt.repository.AirportListRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class BookingServiceTest {

    @Mock
    private AirportListRepository airportListRepo;

    @InjectMocks
    private BookingService bookingService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void searchAirports_withShortQuery_shouldSearchByIataCodeFirst() {
        // Arrange
        String query = "DEL";
        AirportList airport = new AirportList();
        airport.setIataCode("DEL");

        when(airportListRepo.searchByIataCode(query.toUpperCase()))
                .thenReturn(Collections.singletonList(airport));

        // Act
        List<AirportList> result = bookingService.searchAirports(query);

        // Assert
        assertEquals(1, result.size());
        assertEquals("DEL", result.get(0).getIataCode());
    }

    @Test
    void searchAirports_withLongQuery_shouldSearchByNameOrCity() {
        // Arrange
        String query = "Delhi";
        AirportList airport = new AirportList();
        airport.setIataCode("DEL");
        airport.setAirportCity("Delhi");

        when(airportListRepo.searchAirports(query))
                .thenReturn(Collections.singletonList(airport));

        // Act
        List<AirportList> result = bookingService.searchAirports(query);

        // Assert
        assertEquals(1, result.size());
        assertEquals("Delhi", result.get(0).getAirportCity());
    }

    @Test
    void searchAirports_withNoQuery_shouldReturnAllAirports() {
        // Arrange
        AirportList airport1 = new AirportList();
        airport1.setIataCode("DEL");
        AirportList airport2 = new AirportList();
        airport2.setIataCode("BOM");

        when(airportListRepo.findAll())
                .thenReturn(Arrays.asList(airport1, airport2));

        // Act
        List<AirportList> result = bookingService.searchAirports(null);

        // Assert
        assertEquals(2, result.size());
    }


}