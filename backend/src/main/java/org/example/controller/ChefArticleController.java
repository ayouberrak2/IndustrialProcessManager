package org.example.controller;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.example.dto.ErrorResponse;
import org.example.model.ArticleMatiere;
import org.example.repository.ArticleMatiereRepository;

public class ChefArticleController implements HttpHandler {

    private final Gson gson = new Gson();
    private final ArticleMatiereRepository repository = new ArticleMatiereRepository();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        addCorsHeaders(exchange);

        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            exchange.close();
            return;
        }

        try {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();

            if ("GET".equalsIgnoreCase(method) && "/api/chef/articles".equals(path)) {
                Map<String, Object> response = new HashMap<>();
                response.put("articles", repository.findAll());
                sendJson(exchange, 200, response);
                return;
            }

            if ("POST".equalsIgnoreCase(method) && "/api/chef/articles".equals(path)) {
                ArticleMatiere article = readArticle(exchange);

                if (!isValid(article)) {
                    sendJson(exchange, 400, new ErrorResponse("Donnees article invalides"));
                    return;
                }

                ArticleMatiere created = repository.create(article);
                sendJson(exchange, created == null ? 400 : 201, created == null ? new ErrorResponse("Impossible de creer article") : created);
                return;
            }

            if ("PUT".equalsIgnoreCase(method)) {
                int id = getIdFromPath(path);
                ArticleMatiere article = readArticle(exchange);

                if (!isValid(article)) {
                    sendJson(exchange, 400, new ErrorResponse("Donnees article invalides"));
                    return;
                }

                ArticleMatiere updated = repository.update(id, article);
                sendJson(exchange, updated == null ? 404 : 200, updated == null ? new ErrorResponse("Article non trouve") : updated);
                return;
            }

            if ("DELETE".equalsIgnoreCase(method)) {
                int id = getIdFromPath(path);
                boolean deleted = repository.delete(id);
                sendJson(exchange, deleted ? 200 : 400, deleted ? Map.of("success", true) : new ErrorResponse("Article non supprime. Il est peut etre utilise dans un flux ou un lot."));
                return;
            }

            sendJson(exchange, 404, new ErrorResponse("Route articles introuvable"));
        } catch (Exception exception) {
            exception.printStackTrace();
            sendJson(exchange, 500, new ErrorResponse("Erreur serveur : " + exception.getMessage()));
        }
    }

    private ArticleMatiere readArticle(HttpExchange exchange) throws IOException {
        return gson.fromJson(
                new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8),
                ArticleMatiere.class
        );
    }

    private boolean isValid(ArticleMatiere article) {
        return article != null
                && !isBlank(article.getNomArticle())
                && !isBlank(article.getCategorie())
                && !isBlank(article.getUniteStandard())
                && article.getDensiteStandard() > 0;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private int getIdFromPath(String path) {
        return Integer.parseInt(path.replace("/api/chef/articles/", ""));
    }

    private void addCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
    }

    private void sendJson(HttpExchange exchange, int status, Object body) throws IOException {
        byte[] response = gson.toJson(body).getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(status, response.length);

        try (OutputStream output = exchange.getResponseBody()) {
            output.write(response);
        }
    }
}
