package org.example.service;

import org.example.dto.LoginRequest;
import org.example.dto.UserDto;
import org.example.model.User;
import org.example.repository.UserRepository;
import org.example.security.PasswordHasher;

public class AuthService {

    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserDto authenticate(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername());

        if (user == null) {
            return null;
        }

        // Le mot de passe envoye par l'utilisateur est en texte normal.
        // Exemple : admin123
        String passwordFromForm = request.getPassword();

        // Dans la base de donnees, on ne stocke pas admin123 directement.
        // On stocke son hash dans la colonne password_hash.
        String passwordHashFromDatabase = user.getPasswordHash();

        // Donc on hash aussi le mot de passe saisi.
        // Puis on compare : hash saisi == hash dans la base.
        String passwordHashFromForm = PasswordHasher.hashPassword(passwordFromForm);
        boolean passwordIsCorrect = passwordHashFromForm.equals(passwordHashFromDatabase);

        if (!passwordIsCorrect) {
            return null;
        }

        return UserDto.fromModel(user);
    }
}
