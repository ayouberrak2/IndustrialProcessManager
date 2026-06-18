package org.example.controller;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.example.dto.ErrorResponse;
import org.example.service.ChefDashboardService;

public class ChefDashboardController implements HttpHandler {

    private final ChefDashboardService chefDashboardService;
    private final Gson gson = new Gson();

    public ChefDashboardController(ChefDashboardService chefDashboardService) {
        this.chefDashboardService = chefDashboardService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        addCorsHeaders(exchange);

        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            exchange.close();
            return;
        }

        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendJson(exchange, 405, new ErrorResponse("Methode non autorisee"));
            return;
        }

        int atelierId = getAtelierId(exchange);
        sendJson(exchange, 200, chefDashboardService.getDashboard(atelierId));
    }

    private int getAtelierId(HttpExchange exchange) {
        String query = exchange.getRequestURI().getQuery();

        if (query == null || query.isBlank()) {
            return 0;
        }

        String[] parameters = query.split("&");

        for (String parameter : parameters) {
            String[] parts = parameter.split("=");

            if (parts.length == 2 && "atelierId".equals(parts[0])) {
                try {
                    return Integer.parseInt(parts[1]);
                } catch (NumberFormatException exception) {
                    return 0;
                }
            }
        }

        return 0;
    }

    private void addCorsHeaders(HttpExchange exchange) {
        // Allow all origins for local development; adjust for production
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, OPTIONS");
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
