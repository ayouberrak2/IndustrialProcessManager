package org.example.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PasswordHasher {

    public static String hashPassword(String password) {
        try {
            // 1. On choisit l'algorithme de hashage.
            // SHA-256 transforme un texte en une empreinte de 64 caracteres.
            // Important : on ne peut pas retrouver le mot de passe original depuis ce hash.
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");

            // 2. Java ne hash pas directement un String.
            // On convertit donc le mot de passe en tableau de bytes.
            byte[] passwordBytes = password.getBytes(StandardCharsets.UTF_8);

            // 3. On calcule le hash.
            // Le resultat est aussi un tableau de bytes.
            byte[] hashBytes = messageDigest.digest(passwordBytes);

            // 4. Pour stocker le hash dans MySQL, on transforme les bytes en texte hexadecimal.
            // Exemple de caractere hexadecimal : 0, 1, 2, ..., 9, a, b, c, d, e, f.
            StringBuilder hash = new StringBuilder();

            for (byte hashByte : hashBytes) {
                // Un byte peut etre negatif en Java.
                // Avec "& 0xff", on le transforme en nombre positif entre 0 et 255.
                int positiveByte = hashByte & 0xff;

                // On convertit ce nombre en hexadecimal.
                String hexValue = Integer.toHexString(positiveByte);

                // Si la valeur contient un seul caractere, on ajoute un zero devant.
                // Exemple : "a" devient "0a".
                if (hexValue.length() == 1) {
                    hash.append("0");
                }

                hash.append(hexValue);
            }

            // 5. On retourne le hash final sous forme de String.
            // C'est cette valeur qui doit etre comparee avec la colonne password_hash.
            return hash.toString();
        } catch (NoSuchAlgorithmException exception) {
            // Cette erreur est tres rare, car SHA-256 existe normalement dans Java.
            throw new RuntimeException("Impossible de hasher le mot de passe", exception);
        }
    }
}
