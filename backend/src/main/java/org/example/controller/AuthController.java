package org.example.controller;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.example.dto.ErrorResponse;
import org.example.dto.LoginRequest;
import org.example.dto.LoginResponse;
import org.example.dto.UserDto;
import org.example.service.AuthService;

public class AuthController implements HttpHandler {

    private final AuthService authService;
    private final Gson gson = new Gson();

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        addCorsHeaders(exchange);

        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            exchange.close();
            return;
        }

        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendJson(exchange, 405, new ErrorResponse("Methode HTTP non autorisee"));
            return;
        }

        try {
            String requestBody = new String(
                    exchange.getRequestBody().readAllBytes(),
                    StandardCharsets.UTF_8
            );
            LoginRequest request = gson.fromJson(requestBody, LoginRequest.class);

            if (request == null || isBlank(request.getUsername()) || isBlank(request.getPassword())) {
                sendJson(exchange, 400, new ErrorResponse(
                        "Le nom d'utilisateur et le mot de passe sont obligatoires"
                ));
                return;
            }

            UserDto authenticatedUser = authService.authenticate(request);

            if (authenticatedUser == null) {
                sendJson(exchange, 401, new ErrorResponse(
                        "Nom d'utilisateur ou mot de passe incorrect"
                ));
                return;
            }

            sendJson(exchange, 200, new LoginResponse(
                    "Connexion reussie",
                    authenticatedUser
            ));
        } catch (JsonSyntaxException exception) {
            sendJson(exchange, 400, new ErrorResponse("Le JSON envoye est invalide"));
        } catch (Exception exception) {
            exception.printStackTrace();
            sendJson(exchange, 500, new ErrorResponse(
                    "Erreur serveur : " + exception.getMessage()
            ));
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private void addCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "http://localhost:5173");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "POST, OPTIONS");
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
