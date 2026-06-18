package org.example.service;

import java.util.ArrayList;
import java.util.List;

import org.example.dto.UserDto;
import org.example.dto.UserRequest;
import org.example.model.User;
import org.example.repository.UserRepository;
import org.example.security.PasswordHasher;

public class AdminUserService {

    private final UserRepository userRepository;

    public AdminUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<UserDto> getAllUsers() {
        List<User> users = userRepository.findAll();
        List<UserDto> result = new ArrayList<>();

        for (User user : users) {
            result.add(UserDto.fromModel(user));
        }

        return result;
    }

    public boolean createUser(UserRequest request) {
        if (isBlank(request.getUsername()) || isBlank(request.getPassword())
                || isBlank(request.getRole()) || isBlank(request.getEmail())) {
            return false;
        }

        if (roleNeedsAtelier(request.getRole()) && !hasAtelier(request.getAtelierId())) {
            return false;
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPasswordHash(PasswordHasher.hashPassword(request.getPassword()));
        user.setRole(request.getRole());
        user.setEmail(request.getEmail());
        user.setAtelierId(getAtelierIdForCreate(request));

        return userRepository.create(user);
    }

    public boolean updateUser(int id, UserRequest request) {
        if (isBlank(request.getUsername()) || isBlank(request.getRole()) || isBlank(request.getEmail())) {
            return false;
        }

        if (roleNeedsAtelier(request.getRole()) && !hasAtelier(request.getAtelierId())) {
            return false;
        }

        User user = new User();
        user.setId(id);
        user.setUsername(request.getUsername());
        user.setRole(request.getRole());
        user.setEmail(request.getEmail());
        user.setAtelierId(getAtelierIdForUpdate(id, request));

        if (!isBlank(request.getPassword())) {
            user.setPasswordHash(PasswordHasher.hashPassword(request.getPassword()));
        }

        return userRepository.update(user);
    }

    public boolean deleteUser(int id) {
        return userRepository.delete(id);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private boolean roleNeedsAtelier(String role) {
        return "TECHNICIEN_LABO".equals(role);
    }

    private boolean hasAtelier(Integer atelierId) {
        return atelierId != null && atelierId > 0;
    }

    private Integer getAtelierIdForCreate(UserRequest request) {
        if ("TECHNICIEN_LABO".equals(request.getRole())) {
            return request.getAtelierId();
        }

        return null;
    }

    private Integer getAtelierIdForUpdate(int id, UserRequest request) {
        if ("TECHNICIEN_LABO".equals(request.getRole())) {
            return request.getAtelierId();
        }

        if ("CHEF_ATELIER".equals(request.getRole())) {
            User oldUser = userRepository.findById(id);
            if (oldUser != null && "CHEF_ATELIER".equals(oldUser.getRole())) {
                return oldUser.getAtelierId();
            }
        }

        return null;
    }
}
