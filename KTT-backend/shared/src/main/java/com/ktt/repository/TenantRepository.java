package com.ktt.repository;



import com.ktt.entities.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import java.util.List;

@Repository
public interface TenantRepository extends JpaRepository<Tenant, Long> {

    List<Tenant> findByIsActive(Boolean isActive);

    boolean existsByUsernameOrEmail(String username, String email);

    Tenant  findByEmail(String email);
}