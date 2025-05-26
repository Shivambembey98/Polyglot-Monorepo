package com.ktt.requestbuilder;

import com.ktt.dto.AirPriceRequestDto;
import com.ktt.dto.FlightDto;
import com.ktt.requestBuilder.AirPriceRequestBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AirPriceRequestBuilderTest {

    private AirPriceRequestBuilder airPriceRequestBuilder;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

    @BeforeEach
    void setUp() {
        airPriceRequestBuilder = new AirPriceRequestBuilder();
    }

    @Test
    void shouldGenerateValidXmlForSingleFlight() {
        // Arrange
        AirPriceRequestDto request = createBasicRequest();
        request.setAdults(1);
        request.setChildren(0);
        request.setInfants(0);

        String passengerXml = "<com:SearchPassenger Code=\"ADT\" BookingTravelerRef=\"PAX1\"/>";

        // Act
        String result = airPriceRequestBuilder.buildAirPriceRequest(request, passengerXml);

        // Assert
        assertNotNull(result, "Generated XML should not be null");
        assertTrue(result.contains("<air:AirPriceReq"), "Missing AirPriceReq root element");
        assertTrue(result.contains("Carrier=\"AI\""), "Missing carrier information");
        assertTrue(result.contains("FlightNumber=\"101\""), "Missing flight number");
        assertTrue(result.contains("<com:SearchPassenger Code=\"ADT\""), "Missing adult passenger");
    }

    @Test
    void shouldIncludeAllPassengerTypesInXml() {
        // Arrange
        AirPriceRequestDto request = createBasicRequest();
        request.setAdults(1);
        request.setChildren(1);
        request.setInfants(1);

        String passengerXml = "<com:SearchPassenger Code=\"ADT\" BookingTravelerRef=\"PAX1\"/>"
                + "<com:SearchPassenger Code=\"CNN\" Age=\"12\" BookingTravelerRef=\"PAX2\"/>"
                + "<com:SearchPassenger Code=\"INF\" Age=\"1\" PricePTCOnly=\"true\" BookingTravelerRef=\"PAX3\"/>";

        // Act
        String result = airPriceRequestBuilder.buildAirPriceRequest(request, passengerXml);

        // Assert
        assertAll("All passenger types should be included",
                () -> assertTrue(result.contains("Code=\"ADT\""), "Missing adult passenger"),
                () -> assertTrue(result.contains("Code=\"CNN\""), "Missing child passenger"),
                () -> assertTrue(result.contains("Code=\"INF\""), "Missing infant passenger")
        );
    }

    @Test
    void shouldSortFlightsByDepartureTime() {
        // Arrange
        List<FlightDto> flights = Arrays.asList(
                createFlight("FL3", OffsetDateTime.now().plusHours(3)),
                createFlight("FL1", OffsetDateTime.now().plusHours(1)),
                createFlight("FL2", OffsetDateTime.now().plusHours(2))
        );

        // Act
        airPriceRequestBuilder.validateAndSortFlights(flights);

        // Assert
        assertAll("Flights should be sorted by departure time",
                () -> assertEquals("FL1", flights.get(0).getFlightKey(), "First flight incorrect"),
                () -> assertEquals("FL2", flights.get(1).getFlightKey(), "Second flight incorrect"),
                () -> assertEquals("FL3", flights.get(2).getFlightKey(), "Third flight incorrect")
        );
    }

    @Test
    void shouldAcceptValidFlightSequence() {
        // Arrange
        OffsetDateTime now = OffsetDateTime.now();
        List<FlightDto> flights = Arrays.asList(
                createFlight("FL1", now.plusHours(1)),
                createFlight("FL2", now.plusHours(3))
        );

        // Act & Assert
        assertDoesNotThrow(() -> airPriceRequestBuilder.validateAndSortFlights(flights),
                "Should not throw exception for valid flight sequence");
    }

    @Test
    void shouldThrowExceptionForEmptyFlightList() {
        // Arrange
        List<FlightDto> flights = Collections.emptyList();

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> airPriceRequestBuilder.validateAndSortFlights(flights));

        assertEquals("At least one flight segment is required", exception.getMessage());
    }

    private AirPriceRequestDto createBasicRequest() {
        AirPriceRequestDto request = new AirPriceRequestDto();
        FlightDto flight = new FlightDto();
        flight.setFlightKey("Tv5iJZVqWDKAm+9RCAAAAA==");
        flight.setCarrier("AI");
        flight.setFlightNumber("101");
        flight.setOrigin("DEL");
        flight.setDestination("BOM");

        OffsetDateTime departureTime = OffsetDateTime.now().plusDays(1).withOffsetSameInstant(ZoneOffset.of("+05:30"));
        flight.setDepartureTime(departureTime.format(formatter));

        flight.setProviderCode("1G");
        request.setFlights(Collections.singletonList(flight));
        return request;
    }

    private FlightDto createFlight(String flightKey, OffsetDateTime departure) {
        FlightDto flight = new FlightDto();
        flight.setFlightKey(flightKey);
        flight.setCarrier("AI");
        flight.setFlightNumber("999");
        flight.setOrigin("DEL");
        flight.setDestination("BOM");

        OffsetDateTime offsetDeparture = departure.withOffsetSameInstant(ZoneOffset.of("+05:30"));
        flight.setDepartureTime(offsetDeparture.format(formatter));

        flight.setProviderCode("1G");
        return flight;
    }
}
