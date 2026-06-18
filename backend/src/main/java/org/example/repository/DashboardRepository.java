package org.example.repository;

import java.util.ArrayList;
import java.util.List;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.example.config.DatabaseConnection;
import org.example.dto.RecentOperationDto;

public class DashboardRepository {

    public int countEquipements() {
        return countTable("equipement");
    }

    public int countEquipementsByAtelier(int atelierId) {
        if (atelierId <= 0) {
            return countEquipements();
        }

        return countWithAtelier("SELECT COUNT(*) FROM equipement WHERE atelier_id = ?", atelierId);
    }

    public int countOperations() {
        return countTable("operation_process");
    }

    public int countOperationsByAtelier(int atelierId) {
        if (atelierId <= 0) {
            return countOperations();
        }

        String sql = """
                SELECT COUNT(DISTINCT op.id)
                FROM operation_process op
                JOIN operation_equipement oe ON oe.operation_process_id = op.id
                JOIN equipement e ON e.id = oe.equipement_id
                WHERE e.atelier_id = ?
                """;

        return countWithAtelier(sql, atelierId);
    }

    public int countLots() {
        return countTable("lot_production");
    }

    public int countLotsByAtelier(int atelierId) {
        if (atelierId <= 0) {
            return countLots();
        }

        String sql = """
                SELECT COUNT(DISTINCT lp.id)
                FROM lot_production lp
                JOIN operation_process op ON op.id = lp.operation_process_id
                JOIN operation_equipement oe ON oe.operation_process_id = op.id
                JOIN equipement e ON e.id = oe.equipement_id
                WHERE e.atelier_id = ?
                """;

        return countWithAtelier(sql, atelierId);
    }

    public List<RecentOperationDto> findRecentOperations() {
        List<RecentOperationDto> operations = new ArrayList<>();

        String sql = """
                SELECT id, num_ordre_fab, type_operation, statut_operation, date_debut, date_fin
                FROM operation_process
                ORDER BY date_debut DESC, id DESC
                LIMIT 20
                """;

        try {
            DatabaseConnection databaseConnection = DatabaseConnection.getInstance();

            try (Connection connection = databaseConnection.getConnection();
                 PreparedStatement statement = connection.prepareStatement(sql);
                 ResultSet result = statement.executeQuery()) {

                while (result.next()) {
                    RecentOperationDto operation = new RecentOperationDto();

                    operation.setId(result.getInt("id"));
                    operation.setNumOrdreFab(result.getString("num_ordre_fab"));
                    operation.setTypeOperation(result.getString("type_operation"));
                    operation.setStatutOperation(result.getString("statut_operation"));
                    operation.setDateDebut(result.getString("date_debut"));

                    String dateFin = result.getString("date_fin");
                    if (dateFin == null) {
                        dateFin = "Non terminee";
                    }
                    operation.setDateFin(dateFin);

                    operations.add(operation);
                }
            }
        } catch (SQLException exception) {
            System.out.println("Erreur SQL operations recentes : " + exception.getMessage());
        }

        return operations;
    }

    public List<RecentOperationDto> findRecentOperationsByAtelier(int atelierId) {
        if (atelierId <= 0) {
            return findRecentOperations();
        }

        List<RecentOperationDto> operations = new ArrayList<>();

        String sql = """
                SELECT DISTINCT op.id, op.num_ordre_fab, op.type_operation,
                       op.statut_operation, op.date_debut, op.date_fin
                FROM operation_process op
                JOIN operation_equipement oe ON oe.operation_process_id = op.id
                JOIN equipement e ON e.id = oe.equipement_id
                WHERE e.atelier_id = ?
                ORDER BY op.date_debut DESC, op.id DESC
                LIMIT 20
                """;

        try {
            DatabaseConnection databaseConnection = DatabaseConnection.getInstance();

            try (Connection connection = databaseConnection.getConnection();
                 PreparedStatement statement = connection.prepareStatement(sql)) {

                statement.setInt(1, atelierId);

                try (ResultSet result = statement.executeQuery()) {
                    while (result.next()) {
                        operations.add(mapOperation(result));
                    }
                }
            }
        } catch (SQLException exception) {
            System.out.println("Erreur SQL operations recentes atelier : " + exception.getMessage());
        }

        return operations;
    }

    public RecentOperationDto findActiveOperationByAtelier(int atelierId) {
        RecentOperationDto activeOperation = findNotFinishedOperationByAtelier(atelierId);

        if (activeOperation != null) {
            return activeOperation;
        }

        List<RecentOperationDto> recentOperations = findRecentOperationsByAtelier(atelierId);
        if (recentOperations.isEmpty()) {
            return null;
        }

        return recentOperations.get(0);
    }

    private RecentOperationDto findNotFinishedOperationByAtelier(int atelierId) {
        String sqlWithoutAtelier = """
                SELECT id, num_ordre_fab, type_operation, statut_operation, date_debut, date_fin
                FROM operation_process
                WHERE date_fin IS NULL
                   OR UPPER(statut_operation) NOT IN ('TERMINEE', 'TERMINE')
                ORDER BY date_debut DESC, id DESC
                LIMIT 1
                """;

        String sqlWithAtelier = """
                SELECT DISTINCT op.id, op.num_ordre_fab, op.type_operation,
                       op.statut_operation, op.date_debut, op.date_fin
                FROM operation_process op
                JOIN operation_equipement oe ON oe.operation_process_id = op.id
                JOIN equipement e ON e.id = oe.equipement_id
                WHERE e.atelier_id = ?
                  AND (
                    op.date_fin IS NULL
                    OR UPPER(op.statut_operation) NOT IN ('TERMINEE', 'TERMINE')
                  )
                ORDER BY op.date_debut DESC, op.id DESC
                LIMIT 1
                """;

        try {
            DatabaseConnection databaseConnection = DatabaseConnection.getInstance();

            try (Connection connection = databaseConnection.getConnection();
                 PreparedStatement statement = connection.prepareStatement(
                         atelierId > 0 ? sqlWithAtelier : sqlWithoutAtelier
                 )) {

                if (atelierId > 0) {
                    statement.setInt(1, atelierId);
                }

                try (ResultSet result = statement.executeQuery()) {
                    if (result.next()) {
                        return mapOperation(result);
                    }
                }
            }
        } catch (SQLException exception) {
            System.out.println("Erreur SQL operation active : " + exception.getMessage());
        }

        return null;
    }

    private int countTable(String tableName) {
        String sql = "SELECT COUNT(*) FROM " + tableName;

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
            System.out.println("Erreur SQL dashboard count : " + exception.getMessage());
        }

        return 0;
    }

    private int countWithAtelier(String sql, int atelierId) {
        try {
            DatabaseConnection databaseConnection = DatabaseConnection.getInstance();

            try (Connection connection = databaseConnection.getConnection();
                 PreparedStatement statement = connection.prepareStatement(sql)) {

                statement.setInt(1, atelierId);

                try (ResultSet result = statement.executeQuery()) {
                    if (result.next()) {
                        return result.getInt(1);
                    }
                }
            }
        } catch (SQLException exception) {
            System.out.println("Erreur SQL dashboard count atelier : " + exception.getMessage());
        }

        return 0;
    }

    private RecentOperationDto mapOperation(ResultSet result) throws SQLException {
        RecentOperationDto operation = new RecentOperationDto();

        operation.setId(result.getInt("id"));
        operation.setNumOrdreFab(result.getString("num_ordre_fab"));
        operation.setTypeOperation(result.getString("type_operation"));
        operation.setStatutOperation(result.getString("statut_operation"));
        operation.setDateDebut(result.getString("date_debut"));

        String dateFin = result.getString("date_fin");
        if (dateFin == null) {
            dateFin = "Non terminee";
        }
        operation.setDateFin(dateFin);

        return operation;
    }
}
