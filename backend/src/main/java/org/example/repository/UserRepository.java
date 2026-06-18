package org.example.repository;

import java.sql.Connection;
import java.sql.Types;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.example.config.DatabaseConnection;
import org.example.model.User;

public class UserRepository {

    public User findByUsername(String username) {
        String sql = """
                SELECT u.id, u.username, u.password_hash, u.role, u.email,
                       u.atelier_id, a.nom_atelier AS atelier_name
                FROM utilisateur u
                LEFT JOIN atelier a ON u.atelier_id = a.id
                WHERE u.username = ?
                """;

        try {
            DatabaseConnection databaseConnection = DatabaseConnection.getInstance();

            try (Connection connection = databaseConnection.getConnection();
                 PreparedStatement statement = connection.prepareStatement(sql)) {

                statement.setString(1, username);

                try (ResultSet result = statement.executeQuery()) {
                    if (result.next()) {
                        return mapResultToUser(result);
                    }
                }
            }
        } catch (SQLException exception) {
            System.out.println("Erreur SQL findByUsername : " + exception.getMessage());
        }

        return null;
    }

    public User findById(int id) {
        String sql = """
                SELECT u.id, u.username, u.password_hash, u.role, u.email,
                       u.atelier_id, a.nom_atelier AS atelier_name
                FROM utilisateur u
                LEFT JOIN atelier a ON u.atelier_id = a.id
                WHERE u.id = ?
                """;

        try {
            DatabaseConnection databaseConnection = DatabaseConnection.getInstance();

            try (Connection connection = databaseConnection.getConnection();
                 PreparedStatement statement = connection.prepareStatement(sql)) {

                statement.setInt(1, id);

                try (ResultSet result = statement.executeQuery()) {
                    if (result.next()) {
                        return mapResultToUser(result);
                    }
                }
            }
        } catch (SQLException exception) {
            System.out.println("Erreur SQL findById user : " + exception.getMessage());
        }

        return null;
    }

    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        String sql = """
                SELECT u.id, u.username, u.password_hash, u.role, u.email,
                       u.atelier_id, a.nom_atelier AS atelier_name
                FROM utilisateur u
                LEFT JOIN atelier a ON u.atelier_id = a.id
                ORDER BY u.id DESC
                """;

        try {
            DatabaseConnection databaseConnection = DatabaseConnection.getInstance();

            try (Connection connection = databaseConnection.getConnection();
                 PreparedStatement statement = connection.prepareStatement(sql);
                 ResultSet result = statement.executeQuery()) {

                while (result.next()) {
                    users.add(mapResultToUser(result));
                }
            }
        } catch (SQLException exception) {
            System.out.println("Erreur SQL findAll users : " + exception.getMessage());
        }

        return users;
    }

    public boolean create(User user) {
        String sql = """
                INSERT INTO utilisateur (username, password_hash, role, email, atelier_id)
                VALUES (?, ?, ?, ?, ?)
                """;

        try {
            DatabaseConnection databaseConnection = DatabaseConnection.getInstance();

            try (Connection connection = databaseConnection.getConnection();
                 PreparedStatement statement = connection.prepareStatement(sql)) {

                statement.setString(1, user.getUsername());
                statement.setString(2, user.getPasswordHash());
                statement.setString(3, user.getRole());
                statement.setString(4, user.getEmail());
                setAtelierId(statement, 5, user.getAtelierId());

                return statement.executeUpdate() > 0;
            }
        } catch (SQLException exception) {
            System.out.println("Erreur SQL create user : " + exception.getMessage());
            return false;
        }
    }

    public boolean update(User user) {
        String sqlWithoutPassword = """
                UPDATE utilisateur
                SET username = ?, role = ?, email = ?, atelier_id = ?
                WHERE id = ?
                """;
        String sqlWithPassword = """
                UPDATE utilisateur
                SET username = ?, password_hash = ?, role = ?, email = ?, atelier_id = ?
                WHERE id = ?
                """;

        boolean hasNewPassword = user.getPasswordHash() != null && !user.getPasswordHash().isBlank();

        try {
            DatabaseConnection databaseConnection = DatabaseConnection.getInstance();

            try (Connection connection = databaseConnection.getConnection();
                 PreparedStatement statement = connection.prepareStatement(hasNewPassword ? sqlWithPassword : sqlWithoutPassword)) {

                statement.setString(1, user.getUsername());

                if (hasNewPassword) {
                    statement.setString(2, user.getPasswordHash());
                    statement.setString(3, user.getRole());
                    statement.setString(4, user.getEmail());
                    setAtelierId(statement, 5, user.getAtelierId());
                    statement.setInt(6, user.getId());
                } else {
                    statement.setString(2, user.getRole());
                    statement.setString(3, user.getEmail());
                    setAtelierId(statement, 4, user.getAtelierId());
                    statement.setInt(5, user.getId());
                }

                return statement.executeUpdate() > 0;
            }
        } catch (SQLException exception) {
            System.out.println("Erreur SQL update user : " + exception.getMessage());
            return false;
        }
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM utilisateur WHERE id = ?";

        try {
            DatabaseConnection databaseConnection = DatabaseConnection.getInstance();

            try (Connection connection = databaseConnection.getConnection();
                 PreparedStatement statement = connection.prepareStatement(sql)) {

                statement.setInt(1, id);
                return statement.executeUpdate() > 0;
            }
        } catch (SQLException exception) {
            System.out.println("Erreur SQL delete user : " + exception.getMessage());
            return false;
        }
    }

    public int countAll() {
        return countWithSql("SELECT COUNT(*) FROM utilisateur");
    }

    public int countByRole(String role) {
        String sql = "SELECT COUNT(*) FROM utilisateur WHERE role = ?";

        try {
            DatabaseConnection databaseConnection = DatabaseConnection.getInstance();

            try (Connection connection = databaseConnection.getConnection();
                 PreparedStatement statement = connection.prepareStatement(sql)) {

                statement.setString(1, role);

                try (ResultSet result = statement.executeQuery()) {
                    if (result.next()) {
                        return result.getInt(1);
                    }
                }
            }
        } catch (SQLException exception) {
            System.out.println("Erreur SQL countByRole : " + exception.getMessage());
        }

        return 0;
    }

    public int countByRoleAndAtelier(String role, int atelierId) {
        if (atelierId <= 0) {
            return countByRole(role);
        }

        String sql = "SELECT COUNT(*) FROM utilisateur WHERE role = ? AND atelier_id = ?";

        try {
            DatabaseConnection databaseConnection = DatabaseConnection.getInstance();

            try (Connection connection = databaseConnection.getConnection();
                 PreparedStatement statement = connection.prepareStatement(sql)) {

                statement.setString(1, role);
                statement.setInt(2, atelierId);

                try (ResultSet result = statement.executeQuery()) {
                    if (result.next()) {
                        return result.getInt(1);
                    }
                }
            }
        } catch (SQLException exception) {
            System.out.println("Erreur SQL countByRoleAndAtelier : " + exception.getMessage());
        }

        return 0;
    }

    private int countWithSql(String sql) {
        try {
            DatabaseConnection databaseConnection = DatabaseConnection.getInstance();

            try (Connection connection = databaseConnection.getConnection();
                 PreparedStatement statement = connection.prepareStatement(sql);
                 ResultSet result = statement.executeQuery()) {

                if (result.next()) {
                    return result.getInt(1);
                }
            }
        } catch (SQLException exception) {
            System.out.println("Erreur SQL count : " + exception.getMessage());
        }

        return 0;
    }

    private User mapResultToUser(ResultSet result) throws SQLException {
        User user = new User();
        user.setId(result.getInt("id"));
        user.setUsername(result.getString("username"));
        user.setPasswordHash(result.getString("password_hash"));
        user.setRole(result.getString("role"));
        user.setEmail(result.getString("email"));

        Object atelierId = result.getObject("atelier_id");
        if (atelierId != null) {
            user.setAtelierId(((Number) atelierId).intValue());
        }

        user.setAtelierName(result.getString("atelier_name"));
        return user;
    }

    private void setAtelierId(PreparedStatement statement, int index, Integer atelierId) throws SQLException {
        if (atelierId == null || atelierId <= 0) {
            statement.setNull(index, Types.INTEGER);
        } else {
            statement.setInt(index, atelierId);
        }
    }
}
