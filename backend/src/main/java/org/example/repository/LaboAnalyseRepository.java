package org.example.repository;

import org.example.config.DatabaseConnection;
import org.example.dto.AnalyseLaboratoireDto;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LaboAnalyseRepository {

    public List<Map<String, Object>> findByAtelier(int atelierId) {
        List<Map<String, Object>> analyses = new ArrayList<>();

        String sql = """
                SELECT al.id, al.lot_production_id, lp.operation_process_id,
                       op.num_ordre_fab, am.nom_article, lp.statut_qualite,
                       al.taux_p2o5, al.taux_cadmium_ppm, al.solides_suspendu,
                       al.date_analyse
                FROM analyse_laboratoire al
                JOIN lot_production lp ON lp.id = al.lot_production_id
                JOIN operation_process op ON op.id = lp.operation_process_id
                JOIN article_matiere am ON am.id = lp.article_matiere_id
                JOIN operation_equipement oe ON oe.operation_process_id = op.id
                JOIN equipement e ON e.id = oe.equipement_id
                WHERE e.atelier_id = ?
                ORDER BY al.date_analyse DESC, al.id DESC
                """;

        try {
            DatabaseConnection databaseConnection = DatabaseConnection.getInstance();

            try (Connection connection = databaseConnection.getConnection();
                 PreparedStatement statement = connection.prepareStatement(sql)) {

                statement.setInt(1, atelierId);

                try (ResultSet result = statement.executeQuery()) {
                    while (result.next()) {
                        Map<String, Object> analyse = new HashMap<>();
                        analyse.put("id", result.getInt("id"));
                        analyse.put("lotProductionId", result.getInt("lot_production_id"));
                        analyse.put("operationProcessId", result.getInt("operation_process_id"));
                        analyse.put("numOrdreFab", result.getString("num_ordre_fab"));
                        analyse.put("nomArticle", result.getString("nom_article"));
                        analyse.put("statutQualite", result.getString("statut_qualite"));
                        analyse.put("tauxP2O5", result.getFloat("taux_p2o5"));
                        analyse.put("tauxCadmiumPpm", result.getFloat("taux_cadmium_ppm"));
                        analyse.put("solidesSuspendu", result.getFloat("solides_suspendu"));
                        analyse.put("dateAnalyse", result.getString("date_analyse"));
                        analyses.add(analyse);
                    }
                }
            }
        } catch (SQLException exception) {
            System.out.println("Erreur SQL historique analyses labo : " + exception.getMessage());
        }

        return analyses;
    }

    public AnalyseLaboratoireDto saveForAtelier(AnalyseLaboratoireDto analyse, int atelierId) {
        if (!lotBelongsToAtelier(analyse.getLotProductionId(), atelierId)) {
            return null;
        }

        String sql = """
                INSERT INTO analyse_laboratoire
                    (lot_production_id, taux_p2o5, taux_cadmium_ppm, solides_suspendu, date_analyse)
                VALUES (?, ?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                    taux_p2o5 = VALUES(taux_p2o5),
                    taux_cadmium_ppm = VALUES(taux_cadmium_ppm),
                    solides_suspendu = VALUES(solides_suspendu),
                    date_analyse = VALUES(date_analyse)
                """;

        try {
            DatabaseConnection databaseConnection = DatabaseConnection.getInstance();

            try (Connection connection = databaseConnection.getConnection();
                 PreparedStatement statement = connection.prepareStatement(sql)) {

                statement.setInt(1, analyse.getLotProductionId());
                statement.setFloat(2, analyse.getTauxP2O5());
                statement.setFloat(3, analyse.getTauxCadmiumPpm());
                statement.setFloat(4, analyse.getSolidesSuspendu());
                statement.setString(5, analyse.getDateAnalyse());
                statement.executeUpdate();
            }
        } catch (SQLException exception) {
            System.out.println("Erreur SQL save analyse labo : " + exception.getMessage());
            return null;
        }

        return findByLot(analyse.getLotProductionId());
    }

    private AnalyseLaboratoireDto findByLot(int lotProductionId) {
        String sql = """
                SELECT id, lot_production_id, taux_p2o5, taux_cadmium_ppm,
                       solides_suspendu, date_analyse
                FROM analyse_laboratoire
                WHERE lot_production_id = ?
                LIMIT 1
                """;

        try {
            DatabaseConnection databaseConnection = DatabaseConnection.getInstance();

            try (Connection connection = databaseConnection.getConnection();
                 PreparedStatement statement = connection.prepareStatement(sql)) {

                statement.setInt(1, lotProductionId);

                try (ResultSet result = statement.executeQuery()) {
                    if (result.next()) {
                        AnalyseLaboratoireDto analyse = new AnalyseLaboratoireDto();
                        analyse.setId(result.getInt("id"));
                        analyse.setLotProductionId(result.getInt("lot_production_id"));
                        analyse.setTauxP2O5(result.getFloat("taux_p2o5"));
                        analyse.setTauxCadmiumPpm(result.getFloat("taux_cadmium_ppm"));
                        analyse.setSolidesSuspendu(result.getFloat("solides_suspendu"));
                        analyse.setDateAnalyse(result.getString("date_analyse"));
                        return analyse;
                    }
                }
            }
        } catch (SQLException exception) {
            System.out.println("Erreur SQL find analyse lot : " + exception.getMessage());
        }

        return null;
    }

    private boolean lotBelongsToAtelier(Integer lotProductionId, int atelierId) {
        if (lotProductionId == null || lotProductionId <= 0 || atelierId <= 0) {
            return false;
        }

        String sql = """
                SELECT COUNT(*)
                FROM lot_production lp
                JOIN operation_process op ON op.id = lp.operation_process_id
                JOIN operation_equipement oe ON oe.operation_process_id = op.id
                JOIN equipement e ON e.id = oe.equipement_id
                WHERE lp.id = ?
                  AND e.atelier_id = ?
                """;

        try {
            DatabaseConnection databaseConnection = DatabaseConnection.getInstance();

            try (Connection connection = databaseConnection.getConnection();
                 PreparedStatement statement = connection.prepareStatement(sql)) {

                statement.setInt(1, lotProductionId);
                statement.setInt(2, atelierId);

                try (ResultSet result = statement.executeQuery()) {
                    return result.next() && result.getInt(1) > 0;
                }
            }
        } catch (SQLException exception) {
            System.out.println("Erreur SQL validation lot atelier : " + exception.getMessage());
        }

        return false;
    }
}
