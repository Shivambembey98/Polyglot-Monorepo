package com.ktt.controller;

import com.ktt.entities.AirportList;
import com.ktt.service.BookingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class BookingControllerTest {

    @Mock
    private BookingService bookingService;

    @InjectMocks
    private BookingController bookingController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void searchAirports_withQuery_shouldReturnFilteredResults() {
        // Arrange
        String query = "DEL";
        AirportList airport1 = new AirportList();
        airport1.setIataCode("DEL");
        airport1.setAirportName("Indira Gandhi International Airport");

        when(bookingService.searchAirports(query))
                .thenReturn(Arrays.asList(airport1));

        // Act
        ResponseEntity<List<AirportList>> response = bookingController.searchAirports(query);

        // Assert
        assertEquals(1, response.getBody().size());
        assertEquals("DEL", response.getBody().get(0).getIataCode());
    }

    @Test
    void searchAirports_withEmptyQuery_shouldReturnAllResults() {
        // Arrange
        AirportList airport1 = new AirportList();
        airport1.setIataCode("DEL");
        AirportList airport2 = new AirportList();
        airport2.setIataCode("BOM");

        when(bookingService.searchAirports(null))
                .thenReturn(Arrays.asList(airport1, airport2));

        // Act
        ResponseEntity<List<AirportList>> response = bookingController.searchAirports(null);

        // Assert
        assertEquals(2, response.getBody().size());
    }
}