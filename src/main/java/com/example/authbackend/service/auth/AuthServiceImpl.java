package com.example.authbackend.service.auth;

import com.example.authbackend.entity.AuthEntity;
import com.example.authbackend.repository.AuthRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthServiceImpl implements  AuthService{

    @Autowired
    private AuthRepository authRepository;

    @Override
    public Optional<AuthEntity> checkRefreshToken(String refreshToken, String id) {
        return authRepository.findUserByIdAndRefreshToken(id, refreshToken);
    }
}
