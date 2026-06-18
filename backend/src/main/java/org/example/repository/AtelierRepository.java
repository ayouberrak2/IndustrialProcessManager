package org.example.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import org.example.config.DatabaseConnection;
import org.example.model.Atelier;

public class AtelierRepository {

    public List<Atelier> findAll() {
        List<Atelier> ateliers = new ArrayList<>();
        String sql = """
                SELECT a.id, a.code_atelier, a.nom_atelier, a.est_actif,
                       a.chef_atelier_id,
                       chef.username AS chef_atelier_name,
                       chef.email AS chef_atelier_email,
                       COUNT(u.id) AS nombre_users,
                       SUM(CASE WHEN u.role = 'TECHNICIEN_LABO' THEN 1 ELSE 0 END) AS nombre_techniciens
                FROM atelier a
                LEFT JOIN utilisateur chef ON a.chef_atelier_id = chef.id
                LEFT JOIN utilisateur u ON u.atelier_id = a.id
                GROUP BY a.id, a.code_atelier, a.nom_atelier, a.est_actif,
                         a.chef_atelier_id, chef.username, chef.email
                ORDER BY a.id DESC
                """;

        try {
            DatabaseConnection databaseConnection = DatabaseConnection.getInstance();

            try (Connection connection = databaseConnection.getConnection();
                 PreparedStatement statement = connection.prepareStatement(sql);
                 ResultSet result = statement.executeQuery()) {

                while (result.next()) {
                    ateliers.add(mapResultToAtelier(result));
                }
            }
        } catch (SQLException exception) {
            System.out.println("Erreur SQL findAll ateliers : " + exception.getMessage());
        }

        return ateliers;
    }

    public boolean create(Atelier atelier) {
        String sql = """
                INSERT INTO atelier (code_atelier, nom_atelier, est_actif, chef_atelier_id)
                VALUES (?, ?, ?, ?)
                """;

        try {
            DatabaseConnection databaseConnection = DatabaseConnection.getInstance();

            try (Connection connection = databaseConnection.getConnection();
                 PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

                statement.setString(1, atelier.getCodeAtelier());
                statement.setString(2, atelier.getNomAtelier());
                statement.setBoolean(3, atelier.isEstActif());
                setIntegerOrNull(statement, 4, atelier.getChefAtelierId());

                int createdRows = statement.executeUpdate();
                if (createdRows == 0) {
                    return false;
                }

                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int atelierId = generatedKeys.getInt(1);
                        affectChefToAtelier(connection, atelier.getChefAtelierId(), atelierId);
                    }
                }

                return true;
            }
        } catch (SQLException exception) {
            System.out.println("Erreur SQL create atelier : " + exception.getMessage());
            return false;
        }
    }

    public boolean update(Atelier atelier) {
        String sql = """
                UPDATE atelier
                SET code_atelier = ?, nom_atelier = ?, est_actif = ?, chef_atelier_id = ?
                WHERE id = ?
                """;

        try {
            DatabaseConnection databaseConnection = DatabaseConnection.getInstance();

            try (Connection connection = databaseConnection.getConnection();
                 PreparedStatement statement = connection.prepareStatement(sql)) {

                statement.setString(1, atelier.getCodeAtelier());
                statement.setString(2, atelier.getNomAtelier());
                statement.setBoolean(3, atelier.isEstActif());
                setIntegerOrNull(statement, 4, atelier.getChefAtelierId());
                statement.setInt(5, atelier.getId());

                boolean updated = statement.executeUpdate() > 0;
                if (updated) {
                    affectChefToAtelier(connection, atelier.getChefAtelierId(), atelier.getId());
                }

                return updated;
            }
        } catch (SQLException exception) {
            System.out.println("Erreur SQL update atelier : " + exception.getMessage());
            return false;
        }
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM atelier WHERE id = ?";

        try {
            DatabaseConnection databaseConnection = DatabaseConnection.getInstance();

            try (Connection connection = databaseConnection.getConnection()) {
                clearUsersFromAtelier(connection, id);

                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setInt(1, id);
                    return statement.executeUpdate() > 0;
                }

            }
        } catch (SQLException exception) {
            System.out.println("Erreur SQL delete atelier : " + exception.getMessage());
            return false;
        }
    }

    public int countAll() {
        String sql = "SELECT COUNT(*) FROM atelier";

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
            System.out.println("Erreur SQL count ateliers : " + exception.getMessage());
        }

        return 0;
    }

    public boolean chefExists(Integer chefAtelierId) {
        if (chefAtelierId == null || chefAtelierId <= 0) {
            return false;
        }

        String sql = "SELECT COUNT(*) FROM utilisateur WHERE id = ? AND role = 'CHEF_ATELIER'";

        try {
            DatabaseConnection databaseConnection = DatabaseConnection.getInstance();

            try (Connection connection = databaseConnection.getConnection();
                 PreparedStatement statement = connection.prepareStatement(sql)) {

                statement.setInt(1, chefAtelierId);

                try (ResultSet result = statement.executeQuery()) {
                    if (result.next()) {
                        return result.getInt(1) > 0;
                    }
                }
            }
        } catch (SQLException exception) {
            System.out.println("Erreur SQL chefExists : " + exception.getMessage());
        }

        return false;
    }

    public boolean chefAlreadyUsed(Integer chefAtelierId, int currentAtelierId) {
        if (chefAtelierId == null || chefAtelierId <= 0) {
            return false;
        }

        String sql = """
                SELECT COUNT(*)
                FROM atelier
                WHERE chef_atelier_id = ?
                  AND id <> ?
                """;

        try {
            DatabaseConnection databaseConnection = DatabaseConnection.getInstance();

            try (Connection connection = databaseConnection.getConnection();
                 PreparedStatement statement = connection.prepareStatement(sql)) {

                statement.setInt(1, chefAtelierId);
                statement.setInt(2, currentAtelierId);

                try (ResultSet result = statement.executeQuery()) {
                    if (result.next()) {
                        return result.getInt(1) > 0;
                    }
                }
            }
        } catch (SQLException exception) {
            System.out.println("Erreur SQL chefAlreadyUsed : " + exception.getMessage());
        }

        return true;
    }

    private Atelier mapResultToAtelier(ResultSet result) throws SQLException {
        Atelier atelier = new Atelier();
        atelier.setId(result.getInt("id"));
        atelier.setCodeAtelier(result.getString("code_atelier"));
        atelier.setNomAtelier(result.getString("nom_atelier"));
        atelier.setEstActif(result.getBoolean("est_actif"));

        Object chefAtelierId = result.getObject("chef_atelier_id");
        if (chefAtelierId != null) {
            atelier.setChefAtelierId(((Number) chefAtelierId).intValue());
        }

        atelier.setChefAtelierName(result.getString("chef_atelier_name"));
        atelier.setChefAtelierEmail(result.getString("chef_atelier_email"));
        atelier.setNombreUsers(result.getInt("nombre_users"));
        atelier.setNombreTechniciens(result.getInt("nombre_techniciens"));

        return atelier;
    }

    private void setIntegerOrNull(PreparedStatement statement, int index, Integer value) throws SQLException {
        if (value == null || value <= 0) {
            statement.setNull(index, Types.INTEGER);
        } else {
            statement.setInt(index, value);
        }
    }

    private void affectChefToAtelier(Connection connection, Integer chefAtelierId, int atelierId) throws SQLException {
        if (chefAtelierId == null || chefAtelierId <= 0) {
            return;
        }

        String sql = """
                UPDATE utilisateur
                SET atelier_id = ?
                WHERE id = ? AND role = 'CHEF_ATELIER'
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, atelierId);
            statement.setInt(2, chefAtelierId);
            statement.executeUpdate();
        }
    }

    private void clearUsersFromAtelier(Connection connection, int atelierId) throws SQLException {
        String sql = "UPDATE utilisateur SET atelier_id = NULL WHERE atelier_id = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, atelierId);
            statement.executeUpdate();
        }
    }
}
