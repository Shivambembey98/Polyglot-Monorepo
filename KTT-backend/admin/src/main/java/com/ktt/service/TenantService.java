package com.ktt.service;

import com.ktt.dto.TenantRequestDTO;
import com.ktt.dto.TenantResponseDTO;
import com.ktt.entities.Tenant;
import com.ktt.exception.TenantAlreadyExistsException;
import com.ktt.exception.TenantNotFoundException;
import com.ktt.repository.TenantRepository;
import com.ktt.repository.AppConstants;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class TenantService {

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final Logger logger = LoggerFactory.getLogger(TenantService.class);

    @Transactional
    public void createTenant(TenantRequestDTO request) {
        logger.info("Creating tenant: {}", request.getUsername());

        if (tenantRepository.existsByUsernameOrEmail(request.getUsername(), request.getEmail())) {
            logger.warn("Username or email already exists: {}, {}", request.getUsername(), request.getEmail());
            throw new TenantAlreadyExistsException(AppConstants.Tenant.USERNAME_OR_EMAIL_EXISTS);
        }

        Tenant tenant = Tenant.builder()
                .tenantName(request.getTenantName())
                .username(request.getUsername())
                .userPassword(passwordEncoder.encode(request.getUserPassword()))
                .email(request.getEmail())
                .mobile(request.getMobile())
                .companyCode(request.getCompanyCode())
                .companyName(request.getCompanyName())
                .createdBy(request.getCreatedBy() != null ? request.getCreatedBy() : AppConstants.Tenant.DEFAULT_CREATED_BY)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .isActive(true)
                .isDeleted(false)
                .build();

        tenantRepository.save(tenant);
        logger.info("Tenant saved: {}", request.getUsername());
    }

    @Transactional
    public void updateTenant(Long id, TenantRequestDTO request) {
        logger.info("Updating tenant ID: {}", id);
        Tenant existing = tenantRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Tenant not found: {}", id);
                    return new TenantNotFoundException(id);
                });

        if (!existing.getUsername().equals(request.getUsername())
                && tenantRepository.existsByUsernameOrEmail(request.getUsername(), request.getEmail())) {
            logger.warn("Username already exists: {}", request.getUsername());
            throw new TenantAlreadyExistsException(AppConstants.Tenant.USERNAME_EXISTS);
        }

        existing.setTenantName(request.getTenantName());
        existing.setUsername(request.getUsername());
        if (request.getUserPassword() != null && !request.getUserPassword().isEmpty()) {
            existing.setUserPassword(passwordEncoder.encode(request.getUserPassword()));
        }
        existing.setEmail(request.getEmail());
        existing.setMobile(request.getMobile());
        existing.setCompanyCode(request.getCompanyCode());
        existing.setCompanyName(request.getCompanyName());
        existing.setUpdatedAt(LocalDateTime.now());

        tenantRepository.save(existing);
        logger.info("Tenant updated: {}", id);
    }

    @Transactional(readOnly = true)
    public TenantResponseDTO getTenantById(Long id) {
        logger.info("Fetching tenant ID: {}", id);
        Tenant t = tenantRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Tenant not found: {}", id);
                    return new TenantNotFoundException(id);
                });

        return new TenantResponseDTO(t.getId(), t.getTenantName(), t.getUsername(), t.getEmail(), t.getMobile(),
                t.getCompanyCode(), t.getCompanyName(), t.getCreatedAt(), t.getUpdatedAt(),
                t.getUpdatedBy(), t.getIsActive(), t.getIsDeleted());
    }

    @Transactional(readOnly = true)
    public List<TenantResponseDTO> getTenants(Boolean active) {
        logger.info("Fetching tenants (active={})", active);
        List<Tenant> tenants = (active == null) ? tenantRepository.findAll() : tenantRepository.findByIsActive(active);
        logger.info("Fetched {} tenants", tenants.size());

        List<TenantResponseDTO> responses = new ArrayList<>();
        for (Tenant t : tenants) {
            responses.add(new TenantResponseDTO(t.getId(), t.getTenantName(), t.getUsername(), t.getEmail(), t.getMobile(),
                    t.getCompanyCode(), t.getCompanyName(), t.getCreatedAt(), t.getUpdatedAt(),
                    t.getUpdatedBy(), t.getIsActive(), t.getIsDeleted()));
        }
        return responses;
    }

    @Transactional
    public void softDeleteTenant(Long id) {
        logger.info("Soft deleting tenant ID: {}", id);
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Tenant not found for delete: {}", id);
                    return new TenantNotFoundException(id);
                });

        tenant.setIsActive(false);
        tenant.setIsDeleted(true);
        tenantRepository.save(tenant);
        logger.info("Tenant deactivated: {}", id);
    }

    public Map<String, Object> login(String email, String password) {
        logger.info("Login attempt: {}", email);
        Tenant tenant = tenantRepository.findByEmail(email);

        if (tenant == null) {
            logger.warn("Login failed - email not found: {}", email);
            throw new TenantNotFoundException(AppConstants.Tenant.INVALID_EMAIL);
        }

        if (!passwordEncoder.matches(password, tenant.getUserPassword())) {
            logger.warn("Login failed - invalid password for: {}", email);
            throw new TenantNotFoundException(AppConstants.Tenant.INVALID_PASSWORD);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Login successful");
        response.put("tenantId", tenant.getId());
        response.put("username", tenant.getUsername());
        response.put("email", tenant.getTenantName());

        logger.info("Login successful for tenant ID: {}", tenant.getId());
        return response;
    }
}
