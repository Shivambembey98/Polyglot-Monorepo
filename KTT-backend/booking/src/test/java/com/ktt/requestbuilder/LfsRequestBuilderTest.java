package com.ktt.requestbuilder;

import com.ktt.dto.SearchDto;
import com.ktt.dto.SearchLegDto;
import com.ktt.requestBuilder.LfsRequestBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class LfsRequestBuilderTest {

    private LfsRequestBuilder lfsRequestBuilder;

    @BeforeEach
    void setUp() {
        lfsRequestBuilder = new LfsRequestBuilder();
    }

    @Test
    void buildSoapRequestSearch_withOneWayTrip_shouldGenerateCorrectXml() {
        // Arrange
        SearchDto searchDto = new SearchDto();
        searchDto.setOrigin("DEL");
        searchDto.setDestination("BOM");
        searchDto.setFromDate("2025-05-01");
        searchDto.setAdults(1);
        searchDto.setProviderCode("1G");

        // Act
        String result = lfsRequestBuilder.buildSoapRequestSearch(searchDto);

        // Assert
        assertTrue(result.contains("<air:SearchOrigin><com:CityOrAirport Code=\"DEL\"/>"));
        assertTrue(result.contains("<air:SearchDestination><com:CityOrAirport Code=\"BOM\"/>"));
        assertTrue(result.contains("<air:SearchDepTime PreferredTime=\"2025-05-01\"/>"));
        assertTrue(result.contains("<com:SearchPassenger Code=\"ADT\""));
        assertTrue(result.contains("<com:Provider Code=\"1G\" />"));
    }

    @Test
    void buildSoapRequestSearch_withMultiCity_shouldGenerateMultipleLegs() {
        // Arrange
        SearchDto searchDto = new SearchDto();
        SearchLegDto leg1 = new SearchLegDto();
        leg1.setOrigin("DEL");
        leg1.setDestination("BOM");
        leg1.setFromDate("2025-05-01");

        SearchLegDto leg2 = new SearchLegDto();
        leg2.setOrigin("BOM");
        leg2.setDestination("GOI");
        leg2.setFromDate("2025-05-01");

        searchDto.setLegs(Arrays.asList(leg1, leg2));
        searchDto.setAdults(1);

        // Act
        String result = lfsRequestBuilder.buildSoapRequestSearch(searchDto);

        // Assert
        assertTrue(result.contains("<air:SearchAirLeg>"));
        assertTrue(result.split("<air:SearchAirLeg>").length == 3); // 2 legs + closing tag
    }

    @Test
    void buildSoapRequestSearch_withRoundTrip_shouldGenerateReturnLeg() {
        // Arrange
        SearchDto searchDto = new SearchDto();
        searchDto.setOrigin("DEL");
        searchDto.setDestination("BOM");
        searchDto.setFromDate("2025-05-01");
        searchDto.setToDate("2025-05-10");
        searchDto.setAdults(1);

        // Act
        String result = lfsRequestBuilder.buildSoapRequestSearch(searchDto);

        // Assert
        assertTrue(result.contains("<air:SearchAirLeg>"));
        assertTrue(result.split("<air:SearchAirLeg>").length == 3); // 2 legs + closing tag
        assertTrue(result.contains("BOM\"/></air:SearchOrigin><air:SearchDestination><com:CityOrAirport Code=\"DEL\""));
    }
}