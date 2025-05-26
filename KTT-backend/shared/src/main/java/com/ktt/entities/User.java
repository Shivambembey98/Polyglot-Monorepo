package com.ktt.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.ktt.enums.UserRole;


import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Table(name = "users", schema = "ktt")
@Entity()
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String title;

    @NotBlank
    private String login;

    @NotBlank
    private String firstName;

    private String middleName;

    private String lastName;

    private String address;

    @Email
    @NotBlank
    @Column(unique = true)
    private String emailId;

    private String token;

    @Column(columnDefinition = "TIMESTAMP")
    private LocalDateTime tokenCreationDate;

    @NotBlank
    private String identification;

    @JsonIgnore
    @NotBlank
    private String password;

    @JsonIgnore
    private String password2;

    @JsonIgnore
    private String password3;

    @NotBlank
    private String mobileNo;

    private String userType;

    @NotBlank
    private String accountStatus;

    @CreationTimestamp
    private LocalDateTime createDate;

    @UpdateTimestamp
    private LocalDateTime updateDate;

    @UpdateTimestamp
    private LocalDateTime passwordUpdateDate;

    @Max(3)
    @Column(name = "number_of_attempts")
    private int numberOfAttempts;

    private String companyCode;

    private boolean isMailVerified;

    @Column(name = "otp")
    private String otp;


    @Enumerated(EnumType.STRING)
    private UserRole role;

    @Column(name = "is_deleted", nullable = false, columnDefinition = "boolean default false")
    private Boolean isDeleted = false;

    private String city;
    private String state;
    private String nationality;
    private String pinCode;
    private String gender;
    private String dob;
    private String photo;

    public User(String login, String password, String emailId, String title, String firstName, String middleName, String lastName, String address, String identification, String mobileNo, String accountStatus, String companyCode, UserRole role) {
        this.login = login;
        this.password = password;
        this.emailId = emailId;
        this.title = title;
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
        this.address = address;
        this.identification = identification;
        this.mobileNo = mobileNo;
        this.accountStatus = accountStatus;
        this.role = role;
        this.companyCode = companyCode;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (this.role == UserRole.ADMIN) {
            return List.of(new SimpleGrantedAuthority("ROLE_ADMIN"), new SimpleGrantedAuthority("ROLE_USER"));
        }
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getUsername() {
        return login;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public Boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(Boolean deleted) {
        this.isDeleted = deleted;
    }



}