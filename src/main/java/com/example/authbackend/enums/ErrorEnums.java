package com.example.authbackend.enums;

public enum ErrorEnums {
    INVALID_CREDENTIALS("ERR001", "Invalid credentials"),
    USER_NOT_FOUND("ERR002", "User not found"),
    USER_ALREADY_EXISTS("ERR003", "User already exists"),
    INTERNAL_SERVER_ERROR("ERR004", "Internal server error"),
    TOKEN_GENERATION_FAILED("ERR005", "Token generation failed"),
    MISSING_PARAMETER("ERR006", "Missing parameter"),
    UNAUTHORIZED("ERR007", "Unauthorized"),
    INVALID_TOKEN("ERR008", "Invalid token"),
    REFRESH_TOKEN_EXPIRED("ERR009", "Refresh token expired"),
    ACCESS_TOKEN_EXPIRED("ERR010", "Access token expired");

    private final String code;
    private final String message;

    ErrorEnums(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
