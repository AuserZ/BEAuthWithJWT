package com.example.authbackend.controller;

import com.example.authbackend.entity.AuthEntity;
import com.example.authbackend.enums.ErrorEnums;
import com.example.authbackend.model.*;
import com.example.authbackend.repository.AuthRepository;
import com.example.authbackend.util.JwtUtil;
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthRepository authRepository;

    @Autowired
    private JwtUtil jwtUtil;

    ErrorEnums errorEnums;

    private static final Logger LOGGER = LogManager.getLogger(AuthController.class);

    @PostMapping("/login")
    @Operation(summary = "Login to the application",
                responses = {
                    @ApiResponse(responseCode = "200", description = "Login successful"),
                    @ApiResponse(responseCode = "401", description = "Invalid credentials"),
                    @ApiResponse(responseCode = "500", description = "Internal Error")
                })
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) throws Exception {
        LOGGER.info("Login START");
        if(ObjectUtils.isEmpty(loginRequest.getUsername()) || ObjectUtils.isEmpty(loginRequest.getPassword())) {
            LOGGER.error("Missing Request Body");
            throw new IllegalArgumentException("Invalid credentials");
        }

        try{
            AuthEntity user = authRepository.findUserByUsernameAndPassword(loginRequest.getUsername(), loginRequest.getPassword());

            if(ObjectUtils.isEmpty(user))
                return ResponseEntity.status(401).body(
                        Map.of("error", ErrorEnums.INVALID_CREDENTIALS.getCode(),
                                "message", ErrorEnums.INVALID_CREDENTIALS.getMessage())
                );

            String accessToken = jwtUtil.generateAccessToken(user.getId());
            String refreshToken = jwtUtil.generateRefreshToken(user.getId());

            if(StringUtils.isEmpty(accessToken) || StringUtils.isEmpty(refreshToken)) {
                LOGGER.error("Token generation failed");
                throw new Exception("Internal Error");
            }

            LoginResponse loginResponse = new LoginResponse(accessToken, refreshToken, user.getUsername());

            user.setLastLogin();
            user.setRefreshToken(refreshToken);
            user.setLastUpdateRefreshToken();
            authRepository.save(user);

            LOGGER.info("Login END");
            return ResponseEntity.ok(loginResponse);
        } catch (Exception e) {
            throw new Exception("Internal Error");
        }
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh the access token",
                responses = {
                    @ApiResponse(responseCode = "200", description = "Token refreshed"),
                    @ApiResponse(responseCode = "401", description = "Invalid token"),
                    @ApiResponse(responseCode = "500", description = "Internal Error")
                })
    public ResponseEntity<?> refresh(@RequestBody RefreshRequest refreshRequest) throws Exception {
        LOGGER.info("Refresh START");

        if(ObjectUtils.isEmpty(refreshRequest) && !refreshRequest.getRefreshToken().startsWith("Bearer "))
            return ResponseEntity.badRequest().body(
                    Map.of("error", ErrorEnums.MISSING_PARAMETER.getCode(),
                            "message", ErrorEnums.MISSING_PARAMETER.getMessage())
            );
        LOGGER.info("Request not empty");
        LOGGER.info("Request:{}", refreshRequest);

        AuthEntity user = authRepository.findById(refreshRequest.getId()).orElse(null);

        LOGGER.info("user: {}", user);
        if(ObjectUtils.isEmpty(user))
            return ResponseEntity.status(401).body(
                    Map.of("error", ErrorEnums.USER_NOT_FOUND.getCode(),
                            "message", ErrorEnums.USER_NOT_FOUND.getMessage())
            );

        try {
            Claims claimsjwt = jwtUtil.getClaims(refreshRequest.getRefreshToken());
            LOGGER.info("claimsjwt: {}", claimsjwt);
            Date expirationDate = claimsjwt.getExpiration();

            if(ObjectUtils.isEmpty(claimsjwt) && expirationDate.after(new Date()))
                return ResponseEntity.status(401).body(
                        Map.of("error", ErrorEnums.REFRESH_TOKEN_EXPIRED.getCode(),
                                "message", ErrorEnums.REFRESH_TOKEN_EXPIRED.getMessage())
                );

            if(StringUtils.equals(refreshRequest.getRefreshToken(), user.getRefreshToken()))
                return ResponseEntity.status(401).body(
                        Map.of("error", ErrorEnums.INVALID_TOKEN.getCode(),
                                "message", ErrorEnums.INVALID_TOKEN.getMessage())
                );

            LOGGER.info("Refresh token verified");

            String id = claimsjwt.getSubject();
            String newAccessToken = jwtUtil.generateAccessToken(id);
            String newRefreshToken = jwtUtil.generateRefreshToken(id);

            if(StringUtils.isEmpty(newAccessToken)) {
                LOGGER.error("Token generation failed");
                return ResponseEntity.status(500).body(
                        Map.of("error", ErrorEnums.TOKEN_GENERATION_FAILED.getCode(),
                                "message", ErrorEnums.TOKEN_GENERATION_FAILED.getMessage())
                );
            }

            user.setLastUpdateRefreshToken();
            user.setRefreshToken(newRefreshToken);
            authRepository.save(user);
            return ResponseEntity.status(201).body(new LoginResponse(
                    newAccessToken,
                    newRefreshToken,
                    user.getUsername()
            ));

        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Internal Error");
        }
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new user",
                responses = {
                    @ApiResponse(responseCode = "200", description = "User registered"),
                    @ApiResponse(responseCode = "400", description = "Missing parameter"),
                    @ApiResponse(responseCode = "500", description = "Internal Error")
                })
    public ResponseEntity<?> register(@RequestBody RegisterRequest registerRequest) throws Exception {
        LOGGER.info("Register START");
        if(ObjectUtils.isEmpty(registerRequest))
            throw new IllegalArgumentException("Request cannot be empty");

        if(ObjectUtils.isEmpty(registerRequest.getUsername()) || ObjectUtils.isEmpty(registerRequest.getPassword()) || ObjectUtils.isEmpty(registerRequest.getEmail()))
            throw new IllegalArgumentException("Missing parameter");

        try {
            AuthEntity exsitingUser = authRepository.findUserByUsernameAndEmail(registerRequest.getUsername(), registerRequest.getEmail());

            if(!ObjectUtils.isEmpty(exsitingUser))
            {
                LOGGER.error("User already exists");
                return ResponseEntity.status(409).body(
                        Map.of("error", ErrorEnums.USER_ALREADY_EXISTS.getCode(),
                                "message", ErrorEnums.USER_ALREADY_EXISTS.getMessage())
                );
            }

            AuthEntity user = new AuthEntity();
            user.setUsername(registerRequest.getUsername());
            user.setPassword(registerRequest.getPassword());
            user.setEmail(registerRequest.getEmail());
            authRepository.save(user);

            Response responseBody = new Response("User registered");

            LOGGER.info("Register END");
            return ResponseEntity.ok(responseBody);
        } catch (Exception e) {
            throw new Exception("Internal Error");
        }
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout from the application",
                responses = {
                    @ApiResponse(responseCode = "200", description = "Logout successful"),
                    @ApiResponse(responseCode = "401", description = "Invalid token"),
                    @ApiResponse(responseCode = "500", description = "Internal Error")
                })
    public ResponseEntity<?> logout(@RequestBody String accessToken) throws Exception {
        LOGGER.info("Logout START");
        if(StringUtils.isEmpty(accessToken))
            throw new IllegalArgumentException("Token cannot be empty");

        try {
            if(!jwtUtil.validateToken(accessToken))
                throw new IllegalArgumentException("Invalid token");

            LOGGER.info("Logout END");
            return ResponseEntity.ok(Map.of("message", "Logout successful"));
        } catch (Exception e) {
            throw new Exception("Internal Error");
        }
    }
}
