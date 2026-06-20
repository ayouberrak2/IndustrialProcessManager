package org.example.repository;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import org.example.config.DatabaseConnection;
import org.example.dto.AnalyseLaboratoireDto;
import org.example.dto.BilanMassiqueDto;
import org.example.dto.FluxMatiereDto;
import org.example.dto.LotProductionDto;
import org.example.dto.OperationDetailsDto;
import org.example.dto.OperationProcessDto;

public class OperationProcessRepository {

    public List<OperationProcessDto> findByAtelierId(Integer atelierId) {
        List<OperationProcessDto> operations = new ArrayList<>();

        String sql = """
                SELECT DISTINCT op.id, op.num_ordre_fab, op.type_operation,
                       op.statut_operation, op.date_debut, op.date_fin, op.operateur,
                       oe.equipement_id, e.nom_equipement, oe.duree_estimee
                FROM operation_process op
                LEFT JOIN operation_equipement oe ON oe.operation_process_id = op.id
                LEFT JOIN equipement e ON e.id = oe.equipement_id
                """ + (atelierId == null || atelierId <= 0 ? "" : " WHERE e.atelier_id = ? ") + """
                ORDER BY op.date_debut DESC, op.id DESC
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
                        operations.add(mapResultToOperation(result));
                    }
                }
            }
        } catch (SQLException exception) {
            System.out.println("Erreur SQL find operations : " + exception.getMessage());
        }

        return operations;
    }

    public OperationProcessDto findById(int id) {
        String sql = """
                SELECT op.id, op.num_ordre_fab, op.type_operation,
                       op.statut_operation, op.date_debut, op.date_fin, op.operateur,
                       oe.equipement_id, e.nom_equipement, oe.duree_estimee
                FROM operation_process op
                LEFT JOIN operation_equipement oe ON oe.operation_process_id = op.id
                LEFT JOIN equipement e ON e.id = oe.equipement_id
                WHERE op.id = ?
                LIMIT 1
                """;

        try {
            DatabaseConnection databaseConnection = DatabaseConnection.getInstance();

            try (Connection connection = databaseConnection.getConnection();
                 PreparedStatement statement = connection.prepareStatement(sql)) {

                statement.setInt(1, id);

                try (ResultSet result = statement.executeQuery()) {
                    if (result.next()) {
                        return mapResultToOperation(result);
                    }
                }
            }
        } catch (SQLException exception) {
            System.out.println("Erreur SQL find operation by id : " + exception.getMessage());
        }

        return null;
    }

    public OperationDetailsDto findDetailsById(int id) {
        OperationProcessDto operation = findById(id);

        if (operation == null) {
            return null;
        }

        OperationDetailsDto details = new OperationDetailsDto();
        details.setOperation(operation);
        details.setFlux(findFluxByOperation(id));
        details.setBilanMassique(findBilanByOperation(id));
        details.setLots(findLotsByOperation(id));
        details.setAnalyses(findAnalysesByOperation(id));
        return details;
    }

    private List<FluxMatiereDto> findFluxByOperation(int operationId) {
        List<FluxMatiereDto> flux = new ArrayList<>();

        String sql = """
                SELECT fm.id, fm.article_matiere_id, am.nom_article, fm.type_flux,
                       fm.mesure_capteur, fm.mesure_diametre, fm.date_mesure
                FROM flux_matiere fm
                JOIN article_matiere am ON am.id = fm.article_matiere_id
                WHERE fm.operation_process_id = ?
                ORDER BY fm.date_mesure ASC, fm.id ASC
                """;

        try {
            DatabaseConnection databaseConnection = DatabaseConnection.getInstance();

            try (Connection connection = databaseConnection.getConnection();
                 PreparedStatement statement = connection.prepareStatement(sql)) {

                statement.setInt(1, operationId);

                try (ResultSet result = statement.executeQuery()) {
                    while (result.next()) {
                        FluxMatiereDto item = new FluxMatiereDto();
                        item.setId(result.getInt("id"));
                        item.setArticleMatiereId(result.getInt("article_matiere_id"));
                        item.setNomArticle(result.getString("nom_article"));
                        item.setTypeFlux(result.getString("type_flux"));
                        item.setMesureCapteur(result.getFloat("mesure_capteur"));
                        item.setMesureDiametre(result.getFloat("mesure_diametre"));
                        item.setDateMesure(result.getString("date_mesure"));
                        flux.add(item);
                    }
                }
            }
        } catch (SQLException exception) {
            System.out.println("Erreur SQL details flux : " + exception.getMessage());
        }

        return flux;
    }

    private BilanMassiqueDto findBilanByOperation(int operationId) {
        String sql = """
                SELECT id, total_entrees_t, total_sorties_t, ecart_pertes_t,
                       rendement_val, date_calcul
                FROM bilan_massique
                WHERE operation_process_id = ?
                """;

        try {
            DatabaseConnection databaseConnection = DatabaseConnection.getInstance();

            try (Connection connection = databaseConnection.getConnection();
                 PreparedStatement statement = connection.prepareStatement(sql)) {

                statement.setInt(1, operationId);

                try (ResultSet result = statement.executeQuery()) {
                    if (result.next()) {
                        BilanMassiqueDto bilan = new BilanMassiqueDto();
                        bilan.setId(result.getInt("id"));
                        bilan.setTotalEntreesT(result.getFloat("total_entrees_t"));
                        bilan.setTotalSortiesT(result.getFloat("total_sorties_t"));
                        bilan.setEcartPertesT(result.getFloat("ecart_pertes_t"));
                        bilan.setRendementVal(result.getFloat("rendement_val"));
                        bilan.setDateCalcul(result.getString("date_calcul"));
                        return bilan;
                    }
                }
            }
        } catch (SQLException exception) {
            System.out.println("Erreur SQL details bilan : " + exception.getMessage());
        }

        return null;
    }

    private List<LotProductionDto> findLotsByOperation(int operationId) {
        List<LotProductionDto> lots = new ArrayList<>();

        String sql = """
                SELECT lp.id, lp.operation_process_id, op.num_ordre_fab,
                       lp.article_matiere_id, am.nom_article, lp.date, lp.statut_qualite
                FROM lot_production lp
                JOIN operation_process op ON op.id = lp.operation_process_id
                JOIN article_matiere am ON am.id = lp.article_matiere_id
                WHERE lp.operation_process_id = ?
                ORDER BY lp.date DESC, lp.id DESC
                """;

        try {
            DatabaseConnection databaseConnection = DatabaseConnection.getInstance();

            try (Connection connection = databaseConnection.getConnection();
                 PreparedStatement statement = connection.prepareStatement(sql)) {

                statement.setInt(1, operationId);

                try (ResultSet result = statement.executeQuery()) {
                    while (result.next()) {
                        lots.add(mapResultToLot(result));
                    }
                }
            }
        } catch (SQLException exception) {
            System.out.println("Erreur SQL details lots : " + exception.getMessage());
        }

        return lots;
    }

    private List<AnalyseLaboratoireDto> findAnalysesByOperation(int operationId) {
        List<AnalyseLaboratoireDto> analyses = new ArrayList<>();

        String sql = """
                SELECT al.id, al.lot_production_id, al.taux_p2o5,
                       al.taux_cadmium_ppm, al.solides_suspendu, al.date_analyse
                FROM analyse_laboratoire al
                JOIN lot_production lp ON lp.id = al.lot_production_id
                WHERE lp.operation_process_id = ?
                ORDER BY al.date_analyse DESC, al.id DESC
                """;

        try {
            DatabaseConnection databaseConnection = DatabaseConnection.getInstance();

            try (Connection connection = databaseConnection.getConnection();
                 PreparedStatement statement = connection.prepareStatement(sql)) {

                statement.setInt(1, operationId);

                try (ResultSet result = statement.executeQuery()) {
                    while (result.next()) {
                        AnalyseLaboratoireDto analyse = new AnalyseLaboratoireDto();
                        analyse.setId(result.getInt("id"));
                        analyse.setLotProductionId(result.getInt("lot_production_id"));
                        analyse.setTauxP2O5(result.getFloat("taux_p2o5"));
                        analyse.setTauxCadmiumPpm(result.getFloat("taux_cadmium_ppm"));
                        analyse.setSolidesSuspendu(result.getFloat("solides_suspendu"));
                        analyse.setDateAnalyse(result.getString("date_analyse"));
                        analyses.add(analyse);
                    }
                }
            }
        } catch (SQLException exception) {
            System.out.println("Erreur SQL details analyses : " + exception.getMessage());
        }

        return analyses;
    }

    public OperationProcessDto create(OperationProcessDto operation) {
        String operationSql = """
                INSERT INTO operation_process
                (num_ordre_fab, type_operation, statut_operation, date_debut, date_fin, operateur)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        String linkSql = """
                INSERT INTO operation_equipement
                (operation_process_id, equipement_id, duree_estimee)
                VALUES (?, ?, ?)
                """;

        try {
            DatabaseConnection databaseConnection = DatabaseConnection.getInstance();

            try (Connection connection = databaseConnection.getConnection()) {
                connection.setAutoCommit(false);

                try (PreparedStatement operationStatement = connection.prepareStatement(operationSql, Statement.RETURN_GENERATED_KEYS)) {
                    fillOperationStatement(operationStatement, operation);

                    int createdRows = operationStatement.executeUpdate();
                    if (createdRows == 0) {
                        connection.rollback();
                        return null;
                    }

                    int operationId = 0;
                    try (ResultSet generatedKeys = operationStatement.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            operationId = generatedKeys.getInt(1);
                        }
                    }

                    if (operationId == 0 || operation.getEquipementId() == null || operation.getEquipementId() <= 0) {
                        connection.rollback();
                        return null;
                    }

                    try (PreparedStatement linkStatement = connection.prepareStatement(linkSql)) {
                        linkStatement.setInt(1, operationId);
                        linkStatement.setInt(2, operation.getEquipementId());
                        linkStatement.setInt(3, safeDuration(operation.getDureeEstimee()));
                        linkStatement.executeUpdate();
                    }

                    saveEntreeFlux(connection, operationId, operation);

                    if (isClosingOperation(operation)) {
                        saveFlux(
                                connection,
                                operationId,
                                "SORTIE",
                                operation.getSortieArticleMatiereId(),
                                operation.getSortieMesureCapteur(),
                                operation.getSortieMesureDiametre(),
                                operation.getDateFin()
                        );
                        refreshBilanAndLot(connection, operationId, operation);
                    }

                    connection.commit();
                    return findById(operationId);
                } catch (SQLException exception) {
                    connection.rollback();
                    throw exception;
                } finally {
                    connection.setAutoCommit(true);
                }
            }
        } catch (SQLException exception) {
            System.out.println("Erreur SQL create operation : " + exception.getMessage());
            return null;
        }
    }

    public OperationProcessDto update(int id, OperationProcessDto operation) {
        String operationSql = """
                UPDATE operation_process
                SET num_ordre_fab = ?, type_operation = ?, statut_operation = ?,
                    date_debut = ?, date_fin = ?, operateur = ?
                WHERE id = ?
                """;

        try {
            DatabaseConnection databaseConnection = DatabaseConnection.getInstance();

            try (Connection connection = databaseConnection.getConnection()) {
                connection.setAutoCommit(false);

                try (PreparedStatement operationStatement = connection.prepareStatement(operationSql)) {
                    fillOperationStatement(operationStatement, operation);
                    operationStatement.setInt(7, id);

                    int updatedRows = operationStatement.executeUpdate();
                    if (updatedRows == 0) {
                        connection.rollback();
                        return null;
                    }

                    saveOperationEquipement(connection, id, operation);

                    saveEntreeFlux(connection, id, operation);

                    if (isClosingOperation(operation)) {
                        saveFlux(
                                connection,
                                id,
                                "SORTIE",
                                operation.getSortieArticleMatiereId(),
                                operation.getSortieMesureCapteur(),
                                operation.getSortieMesureDiametre(),
                                operation.getDateFin()
                        );
                        refreshBilanAndLot(connection, id, operation);
                    }

                    connection.commit();
                    return findById(id);
                } catch (SQLException exception) {
                    connection.rollback();
                    throw exception;
                } finally {
                    connection.setAutoCommit(true);
                }
            }
        } catch (SQLException exception) {
            System.out.println("Erreur SQL update operation : " + exception.getMessage());
            return null;
        }
    }

    public boolean delete(int id) {
        try {
            DatabaseConnection databaseConnection = DatabaseConnection.getInstance();

            try (Connection connection = databaseConnection.getConnection()) {
                connection.setAutoCommit(false);

                try {
                    executeUpdate(connection, """
                            DELETE al
                            FROM analyse_laboratoire al
                            JOIN lot_production lp ON lp.id = al.lot_production_id
                            WHERE lp.operation_process_id = ?
                            """, id);
                    executeUpdate(connection, "DELETE FROM lot_production WHERE operation_process_id = ?", id);
                    executeUpdate(connection, "DELETE FROM bilan_massique WHERE operation_process_id = ?", id);
                    executeUpdate(connection, "DELETE FROM flux_matiere WHERE operation_process_id = ?", id);
                    executeUpdate(connection, "DELETE FROM operation_equipement WHERE operation_process_id = ?", id);

                    int deletedRows = executeUpdate(connection, "DELETE FROM operation_process WHERE id = ?", id);
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
            System.out.println("Erreur SQL delete operation : " + exception.getMessage());
            return false;
        }
    }

    private void saveOperationEquipement(Connection connection, int operationId, OperationProcessDto operation) throws SQLException {
        if (operation.getEquipementId() == null || operation.getEquipementId() <= 0) {
            return;
        }

        String countSql = "SELECT COUNT(*) FROM operation_equipement WHERE operation_process_id = ?";
        int existingLinks = 0;

        try (PreparedStatement countStatement = connection.prepareStatement(countSql)) {
            countStatement.setInt(1, operationId);

            try (ResultSet result = countStatement.executeQuery()) {
                if (result.next()) {
                    existingLinks = result.getInt(1);
                }
            }
        }

        if (existingLinks > 0) {
            String updateSql = """
                    UPDATE operation_equipement
                    SET equipement_id = ?, duree_estimee = ?
                    WHERE operation_process_id = ?
                    """;

            try (PreparedStatement statement = connection.prepareStatement(updateSql)) {
                statement.setInt(1, operation.getEquipementId());
                statement.setInt(2, safeDuration(operation.getDureeEstimee()));
                statement.setInt(3, operationId);
                statement.executeUpdate();
            }
        } else {
            String insertSql = """
                    INSERT INTO operation_equipement
                    (operation_process_id, equipement_id, duree_estimee)
                    VALUES (?, ?, ?)
                    """;

            try (PreparedStatement statement = connection.prepareStatement(insertSql)) {
                statement.setInt(1, operationId);
                statement.setInt(2, operation.getEquipementId());
                statement.setInt(3, safeDuration(operation.getDureeEstimee()));
                statement.executeUpdate();
            }
        }
    }

    private void saveEntreeFlux(Connection connection, int operationId, OperationProcessDto operation) throws SQLException {
        if (operation.getEntreeFlux() == null || operation.getEntreeFlux().isEmpty()) {
            saveFlux(
                    connection,
                    operationId,
                    "ENTREE",
                    operation.getEntreeArticleMatiereId(),
                    operation.getEntreeMesureCapteur(),
                    operation.getEntreeMesureDiametre(),
                    operation.getDateDebut()
            );
            return;
        }

        executeUpdate(connection, """
                DELETE FROM flux_matiere
                WHERE operation_process_id = ? AND type_flux = 'ENTREE'
                """, operationId);

        for (FluxMatiereDto flux : operation.getEntreeFlux()) {
            insertFlux(
                    connection,
                    operationId,
                    "ENTREE",
                    flux.getArticleMatiereId(),
                    flux.getMesureCapteur(),
                    flux.getMesureDiametre(),
                    operation.getDateDebut()
            );
        }
    }

    private void insertFlux(
            Connection connection,
            int operationId,
            String typeFlux,
            Integer articleMatiereId,
            Float mesureCapteur,
            Float mesureDiametre,
            String dateMesure
    ) throws SQLException {
        if (articleMatiereId == null || articleMatiereId <= 0 || isBlank(dateMesure)) {
            return;
        }

        String insertSql = """
                INSERT INTO flux_matiere
                (operation_process_id, article_matiere_id, type_flux,
                 mesure_capteur, mesure_diametre, date_mesure)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (PreparedStatement statement = connection.prepareStatement(insertSql)) {
            statement.setInt(1, operationId);
            statement.setInt(2, articleMatiereId);
            statement.setString(3, typeFlux);
            statement.setFloat(4, safeFloat(mesureCapteur));
            statement.setFloat(5, safeFloat(mesureDiametre));
            statement.setDate(6, Date.valueOf(dateMesure));
            statement.executeUpdate();
        }
    }

    private void saveFlux(
            Connection connection,
            int operationId,
            String typeFlux,
            Integer articleMatiereId,
            Float mesureCapteur,
            Float mesureDiametre,
            String dateMesure
    ) throws SQLException {
        if (articleMatiereId == null || articleMatiereId <= 0 || isBlank(dateMesure)) {
            return;
        }

        String countSql = """
                SELECT COUNT(*)
                FROM flux_matiere
                WHERE operation_process_id = ? AND type_flux = ?
                """;
        int existingFlux = 0;

        try (PreparedStatement countStatement = connection.prepareStatement(countSql)) {
            countStatement.setInt(1, operationId);
            countStatement.setString(2, typeFlux);

            try (ResultSet result = countStatement.executeQuery()) {
                if (result.next()) {
                    existingFlux = result.getInt(1);
                }
            }
        }

        if (existingFlux > 0) {
            String updateSql = """
                    UPDATE flux_matiere
                    SET article_matiere_id = ?, mesure_capteur = ?,
                        mesure_diametre = ?, date_mesure = ?
                    WHERE operation_process_id = ? AND type_flux = ?
                    """;

            try (PreparedStatement statement = connection.prepareStatement(updateSql)) {
                statement.setInt(1, articleMatiereId);
                statement.setFloat(2, safeFloat(mesureCapteur));
                statement.setFloat(3, safeFloat(mesureDiametre));
                statement.setDate(4, Date.valueOf(dateMesure));
                statement.setInt(5, operationId);
                statement.setString(6, typeFlux);
                statement.executeUpdate();
            }
        } else {
            String insertSql = """
                    INSERT INTO flux_matiere
                    (operation_process_id, article_matiere_id, type_flux,
                     mesure_capteur, mesure_diametre, date_mesure)
                    VALUES (?, ?, ?, ?, ?, ?)
                    """;

            try (PreparedStatement statement = connection.prepareStatement(insertSql)) {
                statement.setInt(1, operationId);
                statement.setInt(2, articleMatiereId);
                statement.setString(3, typeFlux);
                statement.setFloat(4, safeFloat(mesureCapteur));
                statement.setFloat(5, safeFloat(mesureDiametre));
                statement.setDate(6, Date.valueOf(dateMesure));
                statement.executeUpdate();
            }
        }
    }

    private void refreshBilanAndLot(Connection connection, int operationId, OperationProcessDto operation) throws SQLException {
        float totalEntrees = sumFlux(connection, operationId, "ENTREE");
        float totalSorties = sumFlux(connection, operationId, "SORTIE");
        float ecartPertes = totalEntrees - totalSorties;
        float rendement = totalEntrees == 0 ? 0 : (totalSorties / totalEntrees) * 100;
        String dateCalcul = operation.getDateFin();

        if (isBlank(dateCalcul)) {
            dateCalcul = operation.getDateDebut();
        }

        saveBilan(connection, operationId, totalEntrees, totalSorties, ecartPertes, rendement, dateCalcul);
        saveLotFromSortie(connection, operationId, operation, dateCalcul);
    }

    private float sumFlux(Connection connection, int operationId, String typeFlux) throws SQLException {
        String sql = """
                SELECT COALESCE(SUM(mesure_capteur), 0)
                FROM flux_matiere
                WHERE operation_process_id = ? AND type_flux = ?
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, operationId);
            statement.setString(2, typeFlux);

            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return result.getFloat(1);
                }
            }
        }

        return 0;
    }

    private void saveBilan(
            Connection connection,
            int operationId,
            float totalEntrees,
            float totalSorties,
            float ecartPertes,
            float rendement,
            String dateCalcul
    ) throws SQLException {
        String countSql = "SELECT COUNT(*) FROM bilan_massique WHERE operation_process_id = ?";
        int existingBilan = 0;

        try (PreparedStatement countStatement = connection.prepareStatement(countSql)) {
            countStatement.setInt(1, operationId);

            try (ResultSet result = countStatement.executeQuery()) {
                if (result.next()) {
                    existingBilan = result.getInt(1);
                }
            }
        }

        if (existingBilan > 0) {
            String updateSql = """
                    UPDATE bilan_massique
                    SET total_entrees_t = ?, total_sorties_t = ?, ecart_pertes_t = ?,
                        rendement_val = ?, date_calcul = ?
                    WHERE operation_process_id = ?
                    """;

            try (PreparedStatement statement = connection.prepareStatement(updateSql)) {
                statement.setFloat(1, totalEntrees);
                statement.setFloat(2, totalSorties);
                statement.setFloat(3, ecartPertes);
                statement.setFloat(4, rendement);
                statement.setDate(5, Date.valueOf(dateCalcul));
                statement.setInt(6, operationId);
                statement.executeUpdate();
            }
        } else {
            String insertSql = """
                    INSERT INTO bilan_massique
                    (operation_process_id, total_entrees_t, total_sorties_t,
                     ecart_pertes_t, rendement_val, date_calcul)
                    VALUES (?, ?, ?, ?, ?, ?)
                    """;

            try (PreparedStatement statement = connection.prepareStatement(insertSql)) {
                statement.setInt(1, operationId);
                statement.setFloat(2, totalEntrees);
                statement.setFloat(3, totalSorties);
                statement.setFloat(4, ecartPertes);
                statement.setFloat(5, rendement);
                statement.setDate(6, Date.valueOf(dateCalcul));
                statement.executeUpdate();
            }
        }
    }

    private void saveLotFromSortie(
            Connection connection,
            int operationId,
            OperationProcessDto operation,
            String dateLot
    ) throws SQLException {
        if (operation.getSortieArticleMatiereId() == null || operation.getSortieArticleMatiereId() <= 0) {
            return;
        }

        String countSql = "SELECT COUNT(*) FROM lot_production WHERE operation_process_id = ?";
        int existingLots = 0;

        try (PreparedStatement countStatement = connection.prepareStatement(countSql)) {
            countStatement.setInt(1, operationId);

            try (ResultSet result = countStatement.executeQuery()) {
                if (result.next()) {
                    existingLots = result.getInt(1);
                }
            }
        }

        if (existingLots > 0) {
            String updateSql = """
                    UPDATE lot_production
                    SET article_matiere_id = ?, date = ?
                    WHERE operation_process_id = ?
                    """;

            try (PreparedStatement statement = connection.prepareStatement(updateSql)) {
                statement.setInt(1, operation.getSortieArticleMatiereId());
                statement.setDate(2, Date.valueOf(dateLot));
                statement.setInt(3, operationId);
                statement.executeUpdate();
            }
        } else {
            String insertSql = """
                    INSERT INTO lot_production
                    (operation_process_id, article_matiere_id, date, statut_qualite)
                    VALUES (?, ?, ?, ?)
                    """;

            try (PreparedStatement statement = connection.prepareStatement(insertSql)) {
                statement.setInt(1, operationId);
                statement.setInt(2, operation.getSortieArticleMatiereId());
                statement.setDate(3, Date.valueOf(dateLot));
                statement.setString(4, "EN_ATTENTE");
                statement.executeUpdate();
            }
        }
    }

    private void fillOperationStatement(PreparedStatement statement, OperationProcessDto operation) throws SQLException {
        statement.setString(1, operation.getNumOrdreFab());
        statement.setString(2, operation.getTypeOperation());
        statement.setString(3, operation.getStatutOperation());
        statement.setDate(4, Date.valueOf(operation.getDateDebut()));

        if (operation.getDateFin() == null || operation.getDateFin().isBlank()) {
            statement.setNull(5, Types.DATE);
        } else {
            statement.setDate(5, Date.valueOf(operation.getDateFin()));
        }

        statement.setString(6, operation.getOperateur());
    }

    private int executeUpdate(Connection connection, String sql, int id) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            return statement.executeUpdate();
        }
    }

    private int safeDuration(Integer duration) {
        if (duration == null || duration <= 0) {
            return 60;
        }

        return duration;
    }

    private float safeFloat(Float value) {
        if (value == null) {
            return 0;
        }

        return value;
    }

    private boolean isClosingOperation(OperationProcessDto operation) {
        return !isBlank(operation.getDateFin()) || "TERMINEE".equals(operation.getStatutOperation());
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
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

    private OperationProcessDto mapResultToOperation(ResultSet result) throws SQLException {
        OperationProcessDto operation = new OperationProcessDto();
        operation.setId(result.getInt("id"));
        operation.setNumOrdreFab(result.getString("num_ordre_fab"));
        operation.setTypeOperation(result.getString("type_operation"));
        operation.setStatutOperation(result.getString("statut_operation"));
        operation.setDateDebut(result.getString("date_debut"));
        operation.setDateFin(result.getString("date_fin"));
        operation.setOperateur(result.getString("operateur"));

        Object equipementId = result.getObject("equipement_id");
        if (equipementId != null) {
            operation.setEquipementId(((Number) equipementId).intValue());
        }

        operation.setEquipementName(result.getString("nom_equipement"));

        Object dureeEstimee = result.getObject("duree_estimee");
        if (dureeEstimee != null) {
            operation.setDureeEstimee(((Number) dureeEstimee).intValue());
        }

        return operation;
    }
}
