package com.ktt.controller;

import com.ktt.dto.ApiResponse;
import com.ktt.dto.TenantCreationResponse;
import com.ktt.dto.TenantRequestDTO;
import com.ktt.dto.TenantResponseDTO;
import com.ktt.service.TenantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/admin/api/tenants")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TenantController {

    @Autowired
    private TenantService tenantService;

    private static final Logger logger = LoggerFactory.getLogger(TenantController.class);

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<TenantCreationResponse>> createTenant(
            @Valid @RequestBody TenantRequestDTO tenantRequestDTO) {

        logger.info("Received request to create tenant: username={}, email={}",
                tenantRequestDTO.getUsername(), tenantRequestDTO.getEmail());

        tenantService.createTenant(tenantRequestDTO);

        TenantCreationResponse response = TenantCreationResponse.builder()
                .email(tenantRequestDTO.getEmail())
                .username(tenantRequestDTO.getUsername())
                .status("Unverified")
                .build();

        logger.info("Tenant created successfully: {}", response.getUsername());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Tenant created successfully. Please verify your email.", response, HttpStatus.CREATED));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> updateTenant(
            @PathVariable Long id,
            @Valid @RequestBody TenantRequestDTO tenantRequestDTO) {

        logger.info("Received request to update tenant with ID: {}", id);
        tenantService.updateTenant(id, tenantRequestDTO);
        logger.info("Tenant updated successfully: {}", id);

        return ResponseEntity.ok(new ApiResponse<>(true, "Tenant updated successfully", HttpStatus.OK));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TenantResponseDTO>> getTenantById(@PathVariable Long id) {
        logger.info("Fetching tenant details for ID: {}", id);
        TenantResponseDTO response = tenantService.getTenantById(id);
        logger.info("Tenant details retrieved: {}", response.getUsername());
        return ResponseEntity.ok(new ApiResponse<>(true, "Tenant details retrieved successfully", response, HttpStatus.OK));
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<TenantResponseDTO>>> getTenants(
            @RequestParam(required = false) Boolean active) {

        logger.info("Fetching tenants, active={}", active);
        List<TenantResponseDTO> response = tenantService.getTenants(active);
        logger.info("Fetched {} tenants", response.size());

        return ResponseEntity.ok(new ApiResponse<>(true, "Tenants retrieved successfully", response, HttpStatus.OK));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> softDeleteTenant(@PathVariable Long id) {
        logger.info("Received request to deactivate tenant with ID: {}", id);
        tenantService.softDeleteTenant(id);
        logger.info("Tenant deactivated: {}", id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Tenant deactivated successfully", HttpStatus.OK));
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(
            @RequestParam String email,
            @RequestParam String password) {

        logger.info("Login attempt for email: {}", email);
        Map<String, Object> response = tenantService.login(email, password);
        logger.info("Login successful for tenant ID: {}", response.get("tenantId"));
        return ResponseEntity.ok(response);
    }
}