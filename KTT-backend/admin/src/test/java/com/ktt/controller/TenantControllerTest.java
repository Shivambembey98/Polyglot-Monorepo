package com.ktt.controller;

import com.ktt.dto.ApiResponse;
import com.ktt.dto.TenantCreationResponse;
import com.ktt.dto.TenantRequestDTO;
import com.ktt.dto.TenantResponseDTO;
import com.ktt.service.TenantService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TenantControllerTest {

    @Mock
    private TenantService tenantService;

    @InjectMocks
    private TenantController tenantController;

    @Test
    void createTenant_Success() {
        // Arrange
        TenantRequestDTO requestDTO = new TenantRequestDTO(
                "Ankit Sharma Enterprises",
                "ankit_sharma",
                "Password@123",
                "ankit.sharma@example.com",
                "9876543210",
                "COMP001",
                "Sharma Solutions",
                "admin");

        doNothing().when(tenantService).createTenant(any());

        // Act
        ResponseEntity<ApiResponse<TenantCreationResponse>> response =
                tenantController.createTenant(requestDTO);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        assertEquals("ankit_sharma", response.getBody().getData().getUsername());
        assertEquals("ankit.sharma@example.com", response.getBody().getData().getEmail());
        verify(tenantService).createTenant(requestDTO);
    }

    @Test
    void getTenantById_Found() {
        // Arrange
        Long id = 1L;
        TenantResponseDTO mockResponse = TenantResponseDTO.builder()
                .id(id)
                .tenantName("Ankit Sharma Enterprises")
                .username("ankit_sharma")
                .email("ankit.sharma@example.com")
                .mobile("9876543210")
                .companyCode("COMP001")
                .companyName("Sharma Solutions")
                .isActive(true)
                .build();

        when(tenantService.getTenantById(id)).thenReturn(mockResponse);

        // Act
        ResponseEntity<ApiResponse<TenantResponseDTO>> response =
                tenantController.getTenantById(id);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        assertEquals(id, response.getBody().getData().getId());
        assertEquals("Ankit Sharma Enterprises", response.getBody().getData().getTenantName());
        assertEquals("ankit_sharma", response.getBody().getData().getUsername());
        verify(tenantService).getTenantById(id);
    }

    @Test
    void updateTenant_Success() {
        // Arrange
        Long id = 1L;
        TenantRequestDTO requestDTO = new TenantRequestDTO(
                "Ankit Sharma & Co.",
                "ankit_sharma_co",
                null,
                "ankit.sharma.co@example.com",
                "9876543211",
                "COMP002",
                "Sharma & Co. Solutions",
                null);

        doNothing().when(tenantService).updateTenant(eq(id), any());

        // Act
        ResponseEntity<ApiResponse<String>> response =
                tenantController.updateTenant(id, requestDTO);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Tenant updated successfully", response.getBody().getMessage());
        verify(tenantService).updateTenant(id, requestDTO);
    }

    @Test
    void getTenants_WithFilter() {
        // Arrange
        boolean active = true;
        List<TenantResponseDTO> mockList = List.of(
                TenantResponseDTO.builder()
                        .id(1L)
                        .tenantName("Ankit Sharma Enterprises")
                        .isActive(true)
                        .build(),
                TenantResponseDTO.builder()
                        .id(2L)
                        .tenantName("Rahul Patel & Co.")
                        .isActive(true)
                        .build()
        );

        when(tenantService.getTenants(active)).thenReturn(mockList);

        // Act
        ResponseEntity<ApiResponse<List<TenantResponseDTO>>> response =
                tenantController.getTenants(active);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().getData().size());
        assertEquals("Ankit Sharma Enterprises", response.getBody().getData().get(0).getTenantName());
        assertEquals("Rahul Patel & Co.", response.getBody().getData().get(1).getTenantName());
        verify(tenantService).getTenants(active);
    }

    @Test
    void softDeleteTenant_Success() {
        // Arrange
        Long id = 1L;
        doNothing().when(tenantService).softDeleteTenant(id);

        // Act
        ResponseEntity<ApiResponse<Void>> response =
                tenantController.softDeleteTenant(id);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Tenant deactivated successfully", response.getBody().getMessage());
        verify(tenantService).softDeleteTenant(id);
    }
}