package org.example.model;

public class User {

    private Integer id;
    private String username;
    private String passwordHash;
    private String role;
    private String email;
    private Integer atelierId;
    private String atelierName;

    public User() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
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

    public String getAtelierName() {
        return atelierName;
    }

    public void setAtelierName(String atelierName) {
        this.atelierName = atelierName;
    }
}
