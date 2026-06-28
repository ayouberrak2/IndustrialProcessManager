package org.example.config;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseMigration {

    public void run() {
        addAtelierIdToUtilisateur();
        addChefAtelierIdToAtelier();
        addStatutToEquipement();
        attachOldUsersToFirstAtelier();
        attachFirstChefToFirstAtelier();
        cleanDuplicateChefAtelier();
        addUniqueIndexToChefAtelier();
    }

    private void addAtelierIdToUtilisateur() {
        try {
            DatabaseConnection databaseConnection = DatabaseConnection.getInstance();

            try (Connection connection = databaseConnection.getConnection()) {
                if (columnExists(connection, "utilisateur", "atelier_id")) {
                    return;
                }

                try (Statement statement = connection.createStatement()) {
                    statement.executeUpdate("ALTER TABLE utilisateur ADD COLUMN atelier_id INT NULL");
                }
            }
        } catch (SQLException exception) {
            System.out.println("Migration atelier_id : " + exception.getMessage());
        }
    }

    private void attachOldUsersToFirstAtelier() {
        String sql = """
                UPDATE utilisateur
                SET atelier_id = 1
                WHERE role IN ('CHEF_ATELIER', 'TECHNICIEN_LABO')
                  AND atelier_id IS NULL
                  AND EXISTS (SELECT 1 FROM atelier WHERE id = 1)
                """;

        try {
            DatabaseConnection databaseConnection = DatabaseConnection.getInstance();

            try (Connection connection = databaseConnection.getConnection();
                 Statement statement = connection.createStatement()) {

                statement.executeUpdate(sql);
            }
        } catch (SQLException exception) {
            System.out.println("Migration anciens users atelier : " + exception.getMessage());
        }
    }

    private void addChefAtelierIdToAtelier() {
        try {
            DatabaseConnection databaseConnection = DatabaseConnection.getInstance();

            try (Connection connection = databaseConnection.getConnection()) {
                if (columnExists(connection, "atelier", "chef_atelier_id")) {
                    return;
                }

                try (Statement statement = connection.createStatement()) {
                    statement.executeUpdate("ALTER TABLE atelier ADD COLUMN chef_atelier_id INT NULL");
                }
            }
        } catch (SQLException exception) {
            System.out.println("Migration chef_atelier_id : " + exception.getMessage());
        }
    }

    private void addStatutToEquipement() {
        try {
            DatabaseConnection databaseConnection = DatabaseConnection.getInstance();

            try (Connection connection = databaseConnection.getConnection()) {
                if (columnExists(connection, "equipement", "statut_equipement")) {
                    return;
                }

                try (Statement statement = connection.createStatement()) {
                    statement.executeUpdate("ALTER TABLE equipement ADD COLUMN statut_equipement VARCHAR(50) NOT NULL DEFAULT 'OPERATIONNEL'");
                }
            }
        } catch (SQLException exception) {
            System.out.println("Migration statut equipement : " + exception.getMessage());
        }
    }


    private void attachFirstChefToFirstAtelier() {
        String sql = """
                UPDATE atelier
                SET chef_atelier_id = (
                    SELECT id FROM utilisateur
                    WHERE role = 'CHEF_ATELIER'
                    ORDER BY id
                    LIMIT 1
                )
                WHERE id = 1
                  AND chef_atelier_id IS NULL
                  AND EXISTS (
                    SELECT 1 FROM utilisateur
                    WHERE role = 'CHEF_ATELIER'
                  )
                """;

        try {
            DatabaseConnection databaseConnection = DatabaseConnection.getInstance();

            try (Connection connection = databaseConnection.getConnection();
                 Statement statement = connection.createStatement()) {

                statement.executeUpdate(sql);
            }
        } catch (SQLException exception) {
            System.out.println("Migration chef atelier principal : " + exception.getMessage());
        }
    }

    private void cleanDuplicateChefAtelier() {
        String sql = """
                UPDATE atelier a
                JOIN (
                    SELECT chef_atelier_id, MIN(id) AS keep_id
                    FROM atelier
                    WHERE chef_atelier_id IS NOT NULL
                    GROUP BY chef_atelier_id
                ) keeper ON a.chef_atelier_id = keeper.chef_atelier_id
                SET a.chef_atelier_id = NULL
                WHERE a.id <> keeper.keep_id
                """;

        try {
            DatabaseConnection databaseConnection = DatabaseConnection.getInstance();

            try (Connection connection = databaseConnection.getConnection();
                 Statement statement = connection.createStatement()) {

                statement.executeUpdate(sql);
            }
        } catch (SQLException exception) {
            System.out.println("Migration nettoyage chefs doubles : " + exception.getMessage());
        }
    }

    private void addUniqueIndexToChefAtelier() {
        try {
            DatabaseConnection databaseConnection = DatabaseConnection.getInstance();

            try (Connection connection = databaseConnection.getConnection()) {
                if (indexExists(connection, "atelier", "uk_atelier_chef")) {
                    return;
                }

                try (Statement statement = connection.createStatement()) {
                    statement.executeUpdate("ALTER TABLE atelier ADD UNIQUE KEY uk_atelier_chef (chef_atelier_id)");
                }
            }
        } catch (SQLException exception) {
            System.out.println("Migration index chef unique : " + exception.getMessage());
        }
    }

    private boolean columnExists(Connection connection, String tableName, String columnName) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();

        try (ResultSet columns = metaData.getColumns(null, null, tableName, columnName)) {
            return columns.next();
        }
    }

    private boolean indexExists(Connection connection, String tableName, String indexName) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();

        try (ResultSet indexes = metaData.getIndexInfo(null, null, tableName, false, false)) {
            while (indexes.next()) {
                if (indexName.equals(indexes.getString("INDEX_NAME"))) {
                    return true;
                }
            }
        }

        return false;
    }
}
