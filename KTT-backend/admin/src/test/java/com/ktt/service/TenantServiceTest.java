package com.ktt.service;

import com.ktt.dto.TenantRequestDTO;
import com.ktt.dto.TenantResponseDTO;
import com.ktt.entities.Tenant;
import com.ktt.exception.TenantAlreadyExistsException;
import com.ktt.exception.TenantNotFoundException;
import com.ktt.repository.TenantRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TenantServiceTest {

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private TenantService tenantService;

    @Test
    void createTenant_Success() {
        // Arrange
        TenantRequestDTO requestDTO = new TenantRequestDTO(
                "Ankit Enterprises", "ankit", "Ankit@123", "ankit@gmail.com",
                "9876543210", "COMP001", "Ankit Solutions", "admin");

        when(passwordEncoder.encode(any())).thenReturn("encodedPassword");
        when(tenantRepository.existsByUsernameOrEmail(any(), any())).thenReturn(false);
        when(tenantRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        tenantService.createTenant(requestDTO);

        // Assert
        verify(tenantRepository).existsByUsernameOrEmail("ankit", "ankit@gmail.com");
        verify(tenantRepository).save(any(Tenant.class));
    }

    @Test
    void createTenant_DuplicateUsernameOrEmail_ThrowsException() {
        // Arrange
        TenantRequestDTO requestDTO = new TenantRequestDTO(
                "Ankit Enterprises", "ankit", "Ankit@123", "ankit@gmail.com",
                null, null, null, null);

        when(tenantRepository.existsByUsernameOrEmail(any(), any())).thenReturn(true);

        // Act & Assert
        assertThrows(TenantAlreadyExistsException.class, () -> {
            tenantService.createTenant(requestDTO);
        });
    }

    @Test
    void getTenantById_Found() {
        // Arrange
        Long id = 1L;
        Tenant tenant = Tenant.builder()
                .id(id)
                .tenantName("Ankit Enterprises")
                .username("ankit")
                .email("ankit@gmail.com")
                .isActive(true)
                .build();

        when(tenantRepository.findById(id)).thenReturn(Optional.of(tenant));

        // Act
        TenantResponseDTO response = tenantService.getTenantById(id);

        // Assert
        assertNotNull(response);
        assertEquals(id, response.getId());
        assertEquals("ankit", response.getUsername());
        verify(tenantRepository).findById(id);
    }

    @Test
    void getTenantById_NotFound_ThrowsException() {
        // Arrange
        Long id = 99L;
        when(tenantRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(TenantNotFoundException.class, () -> {
            tenantService.getTenantById(id);
        });
    }

    @Test
    void updateTenant_Success() {
        // Arrange
        Long id = 1L;
        Tenant existingTenant = Tenant.builder()
                .id(id)
                .tenantName("Ankit Enterprises")
                .username("ankit")
                .email("ankit@gmail.com")
                .mobile("9876543210")
                .companyCode("COMP001")
                .companyName("Ankit Solutions")
                .isActive(true)
                .build();

        TenantRequestDTO updateDTO = new TenantRequestDTO(
                "Ankit Sharma Enterprises",  // Updated name
                "ankit_sharma",             // Updated username
                null,
                "ankit.sharma@gmail.com",   // Updated email
                "9876543211",               // Updated mobile
                "COMP002",                  // Updated company code
                "Ankit Sharma Solutions",  // Updated company name
                null);

        when(tenantRepository.findById(id)).thenReturn(Optional.of(existingTenant));
        when(tenantRepository.existsByUsernameOrEmail(any(), any())).thenReturn(false);
        when(tenantRepository.save(any())).thenReturn(existingTenant);

        // Act
        tenantService.updateTenant(id, updateDTO);

        // Assert
        verify(tenantRepository).findById(id);
        verify(tenantRepository).save(existingTenant);
        assertEquals("Ankit Sharma Enterprises", existingTenant.getTenantName());
        assertEquals("ankit_sharma", existingTenant.getUsername());
        assertEquals("ankit.sharma@gmail.com", existingTenant.getEmail());
        assertEquals("9876543211", existingTenant.getMobile());
        assertEquals("COMP002", existingTenant.getCompanyCode());
        assertEquals("Ankit Sharma Solutions", existingTenant.getCompanyName());
    }

    @Test
    void softDeleteTenant_Success() {
        // Arrange
        Long id = 1L;
        Tenant tenant = Tenant.builder()
                .id(id)
                .tenantName("Ankit Enterprises")
                .username("ankit")
                .isActive(true)
                .isDeleted(false)
                .build();

        when(tenantRepository.findById(id)).thenReturn(Optional.of(tenant));
        when(tenantRepository.save(any())).thenReturn(tenant);

        // Act
        tenantService.softDeleteTenant(id);

        // Assert
        assertFalse(tenant.getIsActive());
        assertTrue(tenant.getIsDeleted());
        verify(tenantRepository).findById(id);
        verify(tenantRepository).save(tenant);
    }
}