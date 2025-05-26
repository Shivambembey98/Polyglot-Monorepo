package com.ktt.controller;

import com.ktt.config.AirLowFairCacheStore;
import com.ktt.config.AirPriceCacheStore;
import com.ktt.dto.AirCreateReservationDto;
import com.ktt.dto.AirPriceRequestDto;
import com.ktt.dto.SearchDto;
import com.ktt.requestBuilder.*;
import com.ktt.service.SearchService;
import com.ktt.util.AirReservationUtility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SearchControllerTest {

    @Mock
    private SearchService searchService;

    @Mock
    private LfsRequestBuilder lfsRequestBuilder;

    @Mock
    private AirPriceRequestBuilder airPriceRequestBuilder;

    @Mock
    private AirRetrieveRequestBuilder retriveRequestBuilder;

    @Mock
    private AirTicketRequestBuilder airTicketRequestBuilder;

    @Mock
    private AirCreateReservationBuilder airCreateReservationBuilder;

    @Mock
    private AirPriceCacheStore airPriceCacheStore;

    @Mock
    private AirLowFairCacheStore airLowFairCacheStore;

    @InjectMocks
    private SearchController searchController;

    @Mock
    private SeatMapRequestBuilder seatMapRequestBuilder;

    @Mock
    private AirReservationUtility airReservationUtility;

    @BeforeEach
    public void setup() {
        // Explicitly initialize the controller with mocked dependencies
        searchController = new SearchController(
                searchService,
                lfsRequestBuilder,
                airPriceRequestBuilder,
                retriveRequestBuilder,
                airTicketRequestBuilder,
                airCreateReservationBuilder,
                seatMapRequestBuilder,
                airReservationUtility

        );

        // Manually set the cache stores since they're field-injected
        searchController.airPriceCacheStore = airPriceCacheStore;
        searchController.airLowFairCacheStore = airLowFairCacheStore;
    }

    @Test
    void testAirPriceEndpoint() {
        // Setup test data
        AirPriceRequestDto dto = new AirPriceRequestDto();
        dto.setSessionId("test-session");

        // Mock dependencies
        String cachedXml = "<air:AirPricingModifiers></air:AirPricingModifiers>";
        when(airLowFairCacheStore.get("test-session")).thenReturn(cachedXml);

        String mockRequest = "<airPriceRequest/>";
        when(airPriceRequestBuilder.buildAirPriceRequest(dto, cachedXml)).thenReturn(mockRequest);

        String mockResponse = "<air:AirPricingSolution TotalPrice=\"INR4000\"/>";
        when(searchService.streamSearchResponse(mockRequest)).thenReturn(Flux.just(mockResponse));

        doNothing().when(airPriceCacheStore).store(anyString(), anyString());

        // Execute and verify
        StepVerifier.create(searchController.getAirPrice(dto))
                .expectNextMatches(xml ->
                        xml.contains("AirPriceResponse") &&
                                xml.contains("INR4000") &&
                                xml.contains("test-session"))
                .verifyComplete();

        // Verify interactions
        verify(airLowFairCacheStore).get("test-session");
        verify(airPriceRequestBuilder).buildAirPriceRequest(dto, cachedXml);
        verify(searchService).streamSearchResponse(mockRequest);
        verify(airPriceCacheStore).store(anyString(), anyString());
    }

    // Other test methods remain similar with proper mock setup
    @Test
    void testLowFareSearchEndpoint() {
        SearchDto searchDto = new SearchDto();
        searchDto.setOrigin("DEL");
        searchDto.setDestination("BOM");
        searchDto.setFromDate("2025-04-25");

        when(lfsRequestBuilder.buildSoapRequestSearch(searchDto))
                .thenReturn("<searchRequest/>");
        when(searchService.streamSearchResponse("<searchRequest/>"))
                .thenReturn(Flux.just("<air:AirPricePoint TotalPrice=\"INR3000\"/>"));
        doNothing().when(airLowFairCacheStore).store(anyString(), anyString());

        StepVerifier.create(searchController.getAirData(searchDto))
                .expectNextMatches(xml -> xml.contains("LowFareSearchResponse"))
                .verifyComplete();
    }

    @Test
    void testCreateReservation() {
        AirCreateReservationDto dto = new AirCreateReservationDto();
        dto.setSessionIdAirPrice("price-session-id");
        dto.setSessionIdLfs("lfs-session-id");

        when(airPriceCacheStore.get("price-session-id"))
                .thenReturn("<cachedAirPriceXml/>");
        when(airLowFairCacheStore.get("lfs-session-id"))
                .thenReturn("<cachedLfsXml/>");
        when(airCreateReservationBuilder.buildAirCreateReservationRequest(
                any(), any(), any(), any(), any(), any()))
                .thenReturn("<reservationRequest/>");
        when(searchService.streamSearchResponse("<reservationRequest/>"))
                .thenReturn(Flux.just("<reservationResponse>Success</reservationResponse>"));

        StepVerifier.create(searchController.createReservation(dto))
                .expectNextMatches(xml -> xml.contains("Success"))
                .verifyComplete();
    }
}