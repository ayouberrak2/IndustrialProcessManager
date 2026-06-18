package org.example.dto;

import org.example.model.User;

public class UserDto {

    private final Integer id;
    private final String username;
    private final String role;
    private final String email;
    private final Integer atelierId;
    private final String atelierName;

    public UserDto(Integer id, String username, String role, String email, Integer atelierId, String atelierName) {
        this.id = id;
        this.username = username;
        this.role = role;
        this.email = email;
        this.atelierId = atelierId;
        this.atelierName = atelierName;
    }

    public static UserDto fromModel(User user) {
        return new UserDto(
                user.getId(),
                user.getUsername(),
                user.getRole(),
                user.getEmail(),
                user.getAtelierId(),
                user.getAtelierName()
        );
    }

    public Integer getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role;
    }

    public String getEmail() {
        return email;
    }

    public Integer getAtelierId() {
        return atelierId;
    }

    public String getAtelierName() {
        return atelierName;
    }
}
