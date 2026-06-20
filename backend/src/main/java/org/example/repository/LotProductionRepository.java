package org.example.repository;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.example.config.DatabaseConnection;
import org.example.dto.LotProductionDto;

public class LotProductionRepository {

    public List<LotProductionDto> findByAtelierId(Integer atelierId) {
        List<LotProductionDto> lots = new ArrayList<>();

        String sql = """
                SELECT DISTINCT lp.id, lp.operation_process_id, op.num_ordre_fab,
                       lp.article_matiere_id, am.nom_article, lp.date, lp.statut_qualite
                FROM lot_production lp
                JOIN operation_process op ON op.id = lp.operation_process_id
                JOIN article_matiere am ON am.id = lp.article_matiere_id
                LEFT JOIN operation_equipement oe ON oe.operation_process_id = op.id
                LEFT JOIN equipement e ON e.id = oe.equipement_id
                """ + (atelierId == null || atelierId <= 0 ? "" : " WHERE e.atelier_id = ? ") + """
                ORDER BY lp.date DESC, lp.id DESC
                """;

        try {
            DatabaseConnection databaseConnection = DatabaseConnection.getInstance();

            try (Connection connection = databaseConnection.getConnection();
                 PreparedStatement statement = connection.prepareStatement(sql)) {

                if (atelierId != null && atelierId > 0) {
                    statement.setInt(1, atelierId);
                }

                try (ResultSet result = statement.executeQuery()) {
                    while (result.next()) {
                        lots.add(mapResultToLot(result));
                    }
                }
            }
        } catch (SQLException exception) {
            System.out.println("Erreur SQL find lots : " + exception.getMessage());
        }

        return lots;
    }

    public LotProductionDto findById(int id) {
        String sql = """
                SELECT lp.id, lp.operation_process_id, op.num_ordre_fab,
                       lp.article_matiere_id, am.nom_article, lp.date, lp.statut_qualite
                FROM lot_production lp
                JOIN operation_process op ON op.id = lp.operation_process_id
                JOIN article_matiere am ON am.id = lp.article_matiere_id
                WHERE lp.id = ?
                """;

        try {
            DatabaseConnection databaseConnection = DatabaseConnection.getInstance();

            try (Connection connection = databaseConnection.getConnection();
                 PreparedStatement statement = connection.prepareStatement(sql)) {

                statement.setInt(1, id);

                try (ResultSet result = statement.executeQuery()) {
                    if (result.next()) {
                        return mapResultToLot(result);
                    }
                }
            }
        } catch (SQLException exception) {
            System.out.println("Erreur SQL find lot by id : " + exception.getMessage());
        }

        return null;
    }

    public LotProductionDto create(LotProductionDto lot) {
        String sql = """
                INSERT INTO lot_production
                (operation_process_id, article_matiere_id, date, statut_qualite)
                VALUES (?, ?, ?, ?)
                """;

        try {
            DatabaseConnection databaseConnection = DatabaseConnection.getInstance();

            try (Connection connection = databaseConnection.getConnection();
                 PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

                fillLotStatement(statement, lot);

                int createdRows = statement.executeUpdate();
                if (createdRows == 0) {
                    return null;
                }

                int lotId = 0;
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        lotId = generatedKeys.getInt(1);
                    }
                }

                return findById(lotId);
            }
        } catch (SQLException exception) {
            System.out.println("Erreur SQL create lot : " + exception.getMessage());
            return null;
        }
    }

    public LotProductionDto update(int id, LotProductionDto lot) {
        String sql = """
                UPDATE lot_production
                SET operation_process_id = ?, article_matiere_id = ?, date = ?, statut_qualite = ?
                WHERE id = ?
                """;

        try {
            DatabaseConnection databaseConnection = DatabaseConnection.getInstance();

            try (Connection connection = databaseConnection.getConnection();
                 PreparedStatement statement = connection.prepareStatement(sql)) {

                fillLotStatement(statement, lot);
                statement.setInt(5, id);

                int updatedRows = statement.executeUpdate();
                if (updatedRows == 0) {
                    return null;
                }

                return findById(id);
            }
        } catch (SQLException exception) {
            System.out.println("Erreur SQL update lot : " + exception.getMessage());
            return null;
        }
    }

    public boolean delete(int id) {
        try {
            DatabaseConnection databaseConnection = DatabaseConnection.getInstance();

            try (Connection connection = databaseConnection.getConnection()) {
                connection.setAutoCommit(false);

                try {
                    executeUpdate(connection, "DELETE FROM analyse_laboratoire WHERE lot_production_id = ?", id);
                    int deletedRows = executeUpdate(connection, "DELETE FROM lot_production WHERE id = ?", id);
                    connection.commit();
                    return deletedRows > 0;
                } catch (SQLException exception) {
                    connection.rollback();
                    throw exception;
                } finally {
                    connection.setAutoCommit(true);
                }
            }
        } catch (SQLException exception) {
            System.out.println("Erreur SQL delete lot : " + exception.getMessage());
            return false;
        }
    }

    private void fillLotStatement(PreparedStatement statement, LotProductionDto lot) throws SQLException {
        statement.setInt(1, safeId(lot.getOperationProcessId()));
        statement.setInt(2, safeId(lot.getArticleMatiereId()));
        statement.setDate(3, Date.valueOf(lot.getDate()));
        statement.setString(4, lot.getStatutQualite());
    }

    private int executeUpdate(Connection connection, String sql, int id) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            return statement.executeUpdate();
        }
    }

    private int safeId(Integer id) {
        if (id == null) {
            return 0;
        }

        return id;
    }

    private LotProductionDto mapResultToLot(ResultSet result) throws SQLException {
        LotProductionDto lot = new LotProductionDto();
        lot.setId(result.getInt("id"));
        lot.setOperationProcessId(result.getInt("operation_process_id"));
        lot.setNumOrdreFab(result.getString("num_ordre_fab"));
        lot.setArticleMatiereId(result.getInt("article_matiere_id"));
        lot.setNomArticle(result.getString("nom_article"));
        lot.setDate(result.getString("date"));
        lot.setStatutQualite(result.getString("statut_qualite"));
        return lot;
    }
}
