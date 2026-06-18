package org.example.controller;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.example.dto.ErrorResponse;
import org.example.dto.MessageResponse;
import org.example.model.Atelier;
import org.example.service.AtelierService;

public class AdminAtelierController implements HttpHandler {

    private final AtelierService atelierService;
    private final Gson gson = new Gson();

    public AdminAtelierController(AtelierService atelierService) {
        this.atelierService = atelierService;
    }

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

            if ("GET".equalsIgnoreCase(method) && "/api/admin/ateliers".equals(path)) {
                sendJson(exchange, 200, atelierService.getAllAteliers());
                return;
            }

            if ("POST".equalsIgnoreCase(method) && "/api/admin/ateliers".equals(path)) {
                Atelier atelier = readBody(exchange);
                boolean created = atelierService.createAtelier(atelier);
                sendJson(exchange, created ? 201 : 400, new MessageResponse(created ? "Atelier cree" : "Donnees invalides"));
                return;
            }

            if ("PUT".equalsIgnoreCase(method)) {
                int id = getIdFromPath(path);
                Atelier atelier = readBody(exchange);
                boolean updated = atelierService.updateAtelier(id, atelier);
                sendJson(exchange, updated ? 200 : 400, new MessageResponse(updated ? "Atelier modifie" : "Atelier non modifie"));
                return;
            }

            if ("DELETE".equalsIgnoreCase(method)) {
                int id = getIdFromPath(path);
                boolean deleted = atelierService.deleteAtelier(id);
                sendJson(exchange, deleted ? 200 : 400, new MessageResponse(deleted ? "Atelier supprime" : "Atelier non supprime"));
                return;
            }

            sendJson(exchange, 404, new ErrorResponse("Route ateliers introuvable"));
        } catch (JsonSyntaxException exception) {
            sendJson(exchange, 400, new ErrorResponse("JSON invalide"));
        } catch (Exception exception) {
            exception.printStackTrace();
            sendJson(exchange, 500, new ErrorResponse("Erreur serveur : " + exception.getMessage()));
        }
    }

    private Atelier readBody(HttpExchange exchange) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        return gson.fromJson(body, Atelier.class);
    }

    private int getIdFromPath(String path) {
        String idText = path.replace("/api/admin/ateliers/", "");
        return Integer.parseInt(idText);
    }

    private void addCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "http://localhost:5173");
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
