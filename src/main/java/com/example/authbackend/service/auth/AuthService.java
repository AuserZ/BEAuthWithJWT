package com.example.authbackend.service.auth;

import com.example.authbackend.entity.AuthEntity;

import java.util.Optional;


public interface AuthService {
    Optional<AuthEntity> checkRefreshToken(String refreshToken, String id);
}
