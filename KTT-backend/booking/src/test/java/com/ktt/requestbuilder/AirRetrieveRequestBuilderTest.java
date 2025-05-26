package com.ktt.requestbuilder;

import com.ktt.requestBuilder.AirRetrieveRequestBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AirRetrieveRequestBuilderTest {

    private AirRetrieveRequestBuilder airRetrieveRequestBuilder;

    @BeforeEach
    void setUp() {
        airRetrieveRequestBuilder = new AirRetrieveRequestBuilder();
    }

    @Test
    void buildRetrieveRequest_shouldGenerateValidXml() {
        // Arrange
        String locatorCode = "XYZ789";
        String providerCode = "1G";

        // Act
        String result = airRetrieveRequestBuilder.buildRetrieveRequest(locatorCode, providerCode);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("<univ:UniversalRecordRetrieveReq"));
        assertTrue(result.contains("ProviderLocatorCode=\"XYZ789\""));
        assertTrue(result.contains("ProviderCode=\"1G\""));
        assertTrue(result.contains("TargetBranch=\"P7232098\""));
        assertFalse(result.contains("SupplierLocatorCode"));
    }

    @Test
    void buildRetrieveRequest_shouldUseSupplierLocatorForNon1GProviders() {
        // Arrange
        String locatorCode = "ABC123";
        String providerCode = "1P";

        // Act
        String result = airRetrieveRequestBuilder.buildRetrieveRequest(locatorCode, providerCode);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("SupplierLocatorCode=\"ABC123\""));
        assertTrue(result.contains("ProviderCode=\"1P\""));
        assertFalse(result.contains("ProviderLocatorCode"));
    }

    @Test
    void buildRetrieveRequest_shouldHandleEmptyProviderCode() {
        // Arrange
        String locatorCode = "XYZ789";
        String providerCode = "";

        // Act
        String result = airRetrieveRequestBuilder.buildRetrieveRequest(locatorCode, providerCode);

        // Assert
        assertTrue(result.contains("ProviderCode=\"\""));
        assertTrue(result.contains("SupplierLocatorCode=\"XYZ789\""));
    }
}