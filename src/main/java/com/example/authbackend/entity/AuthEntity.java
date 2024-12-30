package com.example.authbackend.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tbl_user")
@Data
@ToString
public class AuthEntity {
    @Id
    @Column(length = 32, unique = true, nullable = false)
    private String id = generateShortUUID();

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "created_datetime", updatable = false)
    private LocalDateTime createdDatetime;

    @Column(name = "last_login")
    private LocalDateTime lastLoginDatetime;

    @Column(name = "refresh_token")
    private String refreshToken;

    @Column(name = "last_update_refresh_token_datetime")
    private LocalDateTime lastUpdateRefreshTokenDatetime;

    @PrePersist
    protected void onCreate(){
        this.createdDatetime = LocalDateTime.now();
    }

    public void setLastLogin(){
        this.lastLoginDatetime = LocalDateTime.now();
    }

    public void setLastUpdateRefreshToken(){
        this.lastUpdateRefreshTokenDatetime = LocalDateTime.now();
    }

    private static String generateShortUUID() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 32);
    }

}
