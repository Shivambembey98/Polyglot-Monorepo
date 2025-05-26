package com.ktt.repository;

import com.ktt.entities.Otp;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OtpRepository extends JpaRepository<Otp, Long> {

    Otp findByEmailIdAndCompanyCode(String emailId, String companyCode);


}
