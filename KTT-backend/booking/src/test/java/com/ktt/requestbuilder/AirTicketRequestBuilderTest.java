package com.ktt.requestbuilder;

import com.ktt.requestBuilder.AirTicketRequestBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AirTicketRequestBuilderTest {

    private AirTicketRequestBuilder airTicketRequestBuilder;

    @BeforeEach
    void setUp() {
        airTicketRequestBuilder = new AirTicketRequestBuilder();
    }

    @Test
    void buildAirTicketRequest_shouldGenerateValidXml() {
        // Arrange
        String locatorCode = "ABC123";

        // Act
        String result = airTicketRequestBuilder.buildAirTicketRequest(locatorCode);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("<AirTicketingReq"));
        assertTrue(result.contains("<AirReservationLocatorCode>ABC123</AirReservationLocatorCode>"));
        assertTrue(result.contains("TargetBranch=\"P7232098\""));
        assertTrue(result.contains("TraceId=\"a0a168a8-1ad3-44d5-b70b-1ccec46b81e8\""));
    }

    @Test
    void buildAirTicketRequest_shouldEscapeSpecialCharacters() {
        // Arrange
        String locatorCode = "1LAM6O";

        // Act
        String result = airTicketRequestBuilder.buildAirTicketRequest(locatorCode);

        // Assert
        assertTrue(result.contains("<AirReservationLocatorCode>1LAM6O</AirReservationLocatorCode>"));
    }
}