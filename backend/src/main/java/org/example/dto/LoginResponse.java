package org.example.dto;

public class LoginResponse {

    private final String message;
    private final UserDto user;

    public LoginResponse(String message, UserDto user) {
        this.message = message;
        this.user = user;
    }

    public String getMessage() {
        return message;
    }

    public UserDto getUser() {
        return user;
    }
}
