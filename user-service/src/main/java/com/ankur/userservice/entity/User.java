package com.ankur.userservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    private String phone;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    void onCreate(){
        this.createdAt = OffsetDateTime.now();
        this.updatedAt = OffsetDateTime.now();
    }

    void onUpdate(){
        this.updatedAt = OffsetDateTime.now();
    }


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(); // no roles for now, empty list
    }

    @Override
    public String getPassword() {
        return passwordHash; // Spring Security needs this to verify BCrypt
    }

    @Override
    public String getUsername() {
        return email; // we use email as the unique identifier
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // not implementing account expiry for now
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // not implementing account locking for now
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // not implementing credential expiry for now
    }

    @Override
    public boolean isEnabled() {
        return isActive; // use your existing isActive field
    }
}
