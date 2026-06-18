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
import org.example.dto.UserRequest;
import org.example.service.AdminUserService;

public class AdminUserController implements HttpHandler {

    private final AdminUserService adminUserService;
    private final Gson gson = new Gson();

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
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

            if ("GET".equalsIgnoreCase(method) && "/api/admin/users".equals(path)) {
                sendJson(exchange, 200, adminUserService.getAllUsers());
                return;
            }

            if ("POST".equalsIgnoreCase(method) && "/api/admin/users".equals(path)) {
                UserRequest request = readBody(exchange);
                boolean created = adminUserService.createUser(request);
                sendJson(exchange, created ? 201 : 400, new MessageResponse(created ? "Utilisateur cree" : "Donnees invalides"));
                return;
            }

            if ("PUT".equalsIgnoreCase(method)) {
                int id = getIdFromPath(path);
                UserRequest request = readBody(exchange);
                boolean updated = adminUserService.updateUser(id, request);
                sendJson(exchange, updated ? 200 : 400, new MessageResponse(updated ? "Utilisateur modifie" : "Utilisateur non modifie"));
                return;
            }

            if ("DELETE".equalsIgnoreCase(method)) {
                int id = getIdFromPath(path);
                boolean deleted = adminUserService.deleteUser(id);
                sendJson(exchange, deleted ? 200 : 400, new MessageResponse(deleted ? "Utilisateur supprime" : "Utilisateur non supprime"));
                return;
            }

            sendJson(exchange, 404, new ErrorResponse("Route users introuvable"));
        } catch (JsonSyntaxException exception) {
            sendJson(exchange, 400, new ErrorResponse("JSON invalide"));
        } catch (Exception exception) {
            exception.printStackTrace();
            sendJson(exchange, 500, new ErrorResponse("Erreur serveur : " + exception.getMessage()));
        }
    }

    private UserRequest readBody(HttpExchange exchange) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        return gson.fromJson(body, UserRequest.class);
    }

    private int getIdFromPath(String path) {
        String idText = path.replace("/api/admin/users/", "");
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
