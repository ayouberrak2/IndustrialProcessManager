package org.example.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.example.config.DatabaseConnection;
import org.example.model.ArticleMatiere;

public class ArticleMatiereRepository {

    public List<ArticleMatiere> findAll() {
        List<ArticleMatiere> articles = new ArrayList<>();

        String sql = """
                SELECT id, nom_article, categorie, unite_standard, densite_standard
                FROM article_matiere
                ORDER BY id DESC
                """;

        try {
            DatabaseConnection databaseConnection = DatabaseConnection.getInstance();

            try (Connection connection = databaseConnection.getConnection();
                 PreparedStatement statement = connection.prepareStatement(sql);
                 ResultSet result = statement.executeQuery()) {

                while (result.next()) {
                    ArticleMatiere article = new ArticleMatiere();
                    article.setId(result.getInt("id"));
                    article.setNomArticle(result.getString("nom_article"));
                    article.setCategorie(result.getString("categorie"));
                    article.setUniteStandard(result.getString("unite_standard"));
                    article.setDensiteStandard(result.getFloat("densite_standard"));
                    articles.add(article);
                }
            }
        } catch (SQLException exception) {
            System.out.println("Erreur SQL find articles : " + exception.getMessage());
        }

        return articles;
    }

    public ArticleMatiere create(ArticleMatiere article) {
        String sql = """
                INSERT INTO article_matiere
                (nom_article, categorie, unite_standard, densite_standard)
                VALUES (?, ?, ?, ?)
                """;

        try {
            DatabaseConnection databaseConnection = DatabaseConnection.getInstance();

            try (Connection connection = databaseConnection.getConnection();
                 PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

                fillStatement(statement, article);

                int createdRows = statement.executeUpdate();
                if (createdRows == 0) {
                    return null;
                }

                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        article.setId(generatedKeys.getInt(1));
                    }
                }

                return article;
            }
        } catch (SQLException exception) {
            System.out.println("Erreur SQL create article : " + exception.getMessage());
            return null;
        }
    }

    public ArticleMatiere update(int id, ArticleMatiere article) {
        String sql = """
                UPDATE article_matiere
                SET nom_article = ?, categorie = ?, unite_standard = ?, densite_standard = ?
                WHERE id = ?
                """;

        try {
            DatabaseConnection databaseConnection = DatabaseConnection.getInstance();

            try (Connection connection = databaseConnection.getConnection();
                 PreparedStatement statement = connection.prepareStatement(sql)) {

                fillStatement(statement, article);
                statement.setInt(5, id);

                int updatedRows = statement.executeUpdate();
                if (updatedRows == 0) {
                    return null;
                }

                article.setId(id);
                return article;
            }
        } catch (SQLException exception) {
            System.out.println("Erreur SQL update article : " + exception.getMessage());
            return null;
        }
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM article_matiere WHERE id = ?";

        try {
            DatabaseConnection databaseConnection = DatabaseConnection.getInstance();

            try (Connection connection = databaseConnection.getConnection();
                 PreparedStatement statement = connection.prepareStatement(sql)) {

                statement.setInt(1, id);
                return statement.executeUpdate() > 0;
            }
        } catch (SQLException exception) {
            System.out.println("Erreur SQL delete article : " + exception.getMessage());
            return false;
        }
    }

    private void fillStatement(PreparedStatement statement, ArticleMatiere article) throws SQLException {
        statement.setString(1, article.getNomArticle());
        statement.setString(2, article.getCategorie());
        statement.setString(3, article.getUniteStandard());
        statement.setFloat(4, article.getDensiteStandard());
    }
}
