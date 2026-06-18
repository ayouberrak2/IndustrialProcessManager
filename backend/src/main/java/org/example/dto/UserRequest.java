package org.example.dto;

public class UserRequest {

    private String username;
    private String password;
    private String role;
    private String email;
    private Integer atelierId;

    public UserRequest() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getAtelierId() {
        return atelierId;
    }

    public void setAtelierId(Integer atelierId) {
        this.atelierId = atelierId;
    }
}
