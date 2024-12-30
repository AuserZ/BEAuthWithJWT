package com.example.authbackend.repository;

import com.example.authbackend.entity.AuthEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface AuthRepository extends JpaRepository<AuthEntity, String> {
    AuthEntity findUserByUsernameAndPassword (String username, String password);
    AuthEntity findUserByUsername(String username);
    AuthEntity findUserByUsernameAndEmail(String username, String email);
    Optional<AuthEntity> findUserByIdAndRefreshToken(String id, String refreshToken);
}
