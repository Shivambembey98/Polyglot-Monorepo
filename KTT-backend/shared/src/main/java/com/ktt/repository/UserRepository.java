package com.ktt.repository;

import com.ktt.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    UserDetails findByLogin(String login);

    @Query(value = "Select login from User where emailId = :emailId")
    String getLoginByEmailId(@Param("emailId") String emailId);

    UserDetails findByLoginAndCompanyCode(String login, String companyCode);

    UserDetails findByEmailIdAndCompanyCode(String emailId, String companyCode);

    /*@Modifying
    @Query(value = "Update User set numberOfAttempts = (Select numberOfAttempts + 1 where login = :login and companyCode = :companyCode)")
    void updateNoOfAttempt(@Param("login") String login, @Param("companyCode") String companyCode);
  */
    @Query(value = "Select password from User where login = :login and companyCode = :companyCode")
    String getPassword(@Param("login") String login, @Param("companyCode") String companyCode);

    @Query(value = "Select accountStatus from User where login = :login and companyCode = :companyCode")
    String getAccountStatus(@Param("login") String login, @Param("companyCode") String companyCode);

    @Query(value = "Select isMailVerified from User where login = :login and companyCode = :companyCode")
    Boolean getMailVerificationStatus(@Param("login") String login, @Param("companyCode") String companyCode);

    @Modifying
    @Query("UPDATE User u SET u.token = :token WHERE u.login = :login AND u.companyCode = :companyCode")
    void updateJwtToken(@Param("token") String token, @Param("login") String login, @Param("companyCode") String companyCode);

    User findByToken(String token);

    User findByEmailIdAndCompanyCodeAndOtp(String email, String companyCode, String otp);


    Optional<User> findUserByLogin(String login);

    Optional<User> findUserByEmailId(String emailId);

    //Find user by login or email (for profile fetching)
    Optional<User> findByLoginOrEmailId(String login, String emailId);

    Optional<User> findByEmailId(String emailId);



    // Add these methods to UserRepository
    List<User> findByIsDeletedTrue();
    List<User> findByIsDeletedFalse();


}
