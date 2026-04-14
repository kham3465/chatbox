package com.vn.nhom2.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.vn.nhom2.enums.Role;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Data

@Entity
@NoArgsConstructor
@Table(name = "users")
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "name")
    private String name;
    @JsonIgnore
    private String password;
    @Column(name = "type_user")
    @Enumerated(EnumType.STRING)
    private Role role;
    @Column(name = "create_time")
    private LocalDateTime createdTime;
    @Column(name = "is_active")
    private Boolean isActive;
    @Column(name = "phone_number", unique = true)
    private String phoneNumber;
    @Column(name = "full_name")
    private String fullName;
    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;
    @Column(name = "sex")
    private String sex;
    @Column(name = "height")
    private Double height;
    @Column(name = "weight")
    private Double weight;
    @Column(name = "blood_group")
    private String bloodGroup;
    @Column(name = "fcm_token")
    private String fcmToken;
    @Column(name = "image_profile")
    private String imageProfile;
    @Override
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()));
    }
    @Override
    @JsonIgnore
    public String getUsername() {
        return name;
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonExpired() {
        return isActive;
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonLocked() {
        return isActive;
    }

    @Override
    @JsonIgnore
    public boolean isCredentialsNonExpired() {
        return isActive;
    }

    @Override
    @JsonIgnore
    public boolean isEnabled() {
        return isActive;
    }
}