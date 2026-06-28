package org.example.repository;

import org.example.config.DatabaseConnection;
import org.example.model.Equipement;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class EquipementRepository {

    public List<Equipement> findByAtelierId(Integer atelierId) {
        List<Equipement> list = new ArrayList<>();
        String sql = "SELECT id, atelier_id, tag_industriel, type_equipement, nom_equipement, statut_equipement FROM equipement" +
                (atelierId == null || atelierId <= 0 ? "" : " WHERE atelier_id = ?") +
                " ORDER BY id DESC";

        try {
            DatabaseConnection db = DatabaseConnection.getInstance();
            try (Connection conn = db.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
                if (atelierId != null && atelierId > 0) stmt.setInt(1, atelierId);

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        Equipement e = map(rs);
                        list.add(e);
                    }
                }
            }
        } catch (SQLException ex) {
            System.out.println("Erreur SQL find equipements : " + ex.getMessage());
        }

        return list;
    }

    public Equipement create(Equipement equipement, Integer atelierId) {
        String sql = "INSERT INTO equipement (atelier_id, tag_industriel, type_equipement, nom_equipement, statut_equipement) VALUES (?, ?, ?, ?, ?)";

        try {
            DatabaseConnection db = DatabaseConnection.getInstance();
            try (Connection conn = db.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setInt(1, atelierId == null ? 0 : atelierId);
                stmt.setString(2, equipement.getTagIndustriel());
                stmt.setString(3, equipement.getTypeEquipement());
                stmt.setString(4, equipement.getNomEquipement());
                stmt.setString(5, normalizeStatut(equipement.getStatutEquipement()));

                int created = stmt.executeUpdate();
                if (created == 0) return null;

                try (ResultSet generated = stmt.getGeneratedKeys()) {
                    if (generated.next()) {
                        equipement.setId(generated.getInt(1));
                    }
                }

                return equipement;
            }
        } catch (SQLException ex) {
            System.out.println("Erreur SQL create equipement : " + ex.getMessage());
            return null;
        }
    }

    public Equipement update(int id, Equipement equipement) {
        String sql = "UPDATE equipement SET tag_industriel = ?, type_equipement = ?, nom_equipement = ?, statut_equipement = ? WHERE id = ?";

        try {
            DatabaseConnection db = DatabaseConnection.getInstance();
            try (Connection conn = db.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, equipement.getTagIndustriel());
                stmt.setString(2, equipement.getTypeEquipement());
                stmt.setString(3, equipement.getNomEquipement());
                stmt.setString(4, normalizeStatut(equipement.getStatutEquipement()));
                stmt.setInt(5, id);

                int updated = stmt.executeUpdate();
                if (updated == 0) return null;

                equipement.setId(id);
                return equipement;
            }
        } catch (SQLException ex) {
            System.out.println("Erreur SQL update equipement : " + ex.getMessage());
            return null;
        }
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM equipement WHERE id = ?";

        try {
            DatabaseConnection db = DatabaseConnection.getInstance();
            try (Connection conn = db.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, id);
                return stmt.executeUpdate() > 0;
            }
        } catch (SQLException ex) {
            System.out.println("Erreur SQL delete equipement : " + ex.getMessage());
            return false;
        }
    }

    private Equipement map(ResultSet rs) throws SQLException {
        Equipement e = new Equipement();
        e.setId(rs.getInt("id"));
        e.setTagIndustriel(rs.getString("tag_industriel"));
        e.setTypeEquipement(rs.getString("type_equipement"));
        e.setNomEquipement(rs.getString("nom_equipement"));
        e.setStatutEquipement(rs.getString("statut_equipement"));
        return e;
    }

    private String normalizeStatut(String statut) {
        if (statut == null || statut.isBlank()) {
            return "OPERATIONNEL";
        }

        if ("EN_PANNE".equalsIgnoreCase(statut)) {
            return "EN_PANNE";
        }

        return "OPERATIONNEL";
    }
}
