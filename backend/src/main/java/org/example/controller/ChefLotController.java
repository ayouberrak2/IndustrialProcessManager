package org.example.controller;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.example.dto.ErrorResponse;
import org.example.dto.LotProductionDto;
import org.example.repository.LotProductionRepository;

public class ChefLotController implements HttpHandler {

    private final Gson gson = new Gson();
    private final LotProductionRepository repository = new LotProductionRepository();

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

            if ("GET".equalsIgnoreCase(method) && "/api/chef/lots".equals(path)) {
                handleGet(exchange);
                return;
            }

            if ("POST".equalsIgnoreCase(method) && "/api/chef/lots".equals(path)) {
                handlePost(exchange);
                return;
            }

            if ("PUT".equalsIgnoreCase(method)) {
                int id = getIdFromPath(path);
                handlePut(exchange, id);
                return;
            }

            if ("DELETE".equalsIgnoreCase(method)) {
                int id = getIdFromPath(path);
                handleDelete(exchange, id);
                return;
            }

            sendJson(exchange, 404, new ErrorResponse("Route lots introuvable"));
        } catch (Exception exception) {
            exception.printStackTrace();
            sendJson(exchange, 500, new ErrorResponse("Erreur serveur : " + exception.getMessage()));
        }
    }

    private void handleGet(HttpExchange exchange) throws IOException {
        Integer atelierId = getAtelierId(exchange);
        List<LotProductionDto> lots = repository.findByAtelierId(atelierId);

        Map<String, Object> response = new HashMap<>();
        response.put("lots", lots);
        sendJson(exchange, 200, response);
    }

    private void handlePost(HttpExchange exchange) throws IOException {
        LotProductionDto lot = readLot(exchange);

        if (!isValid(lot)) {
            sendJson(exchange, 400, new ErrorResponse("Donnees lot invalides"));
            return;
        }

        LotProductionDto created = repository.create(lot);
        if (created == null) {
            sendJson(exchange, 400, new ErrorResponse("Impossible de creer lot"));
            return;
        }

        sendJson(exchange, 201, created);
    }

    private void handlePut(HttpExchange exchange, int id) throws IOException {
        LotProductionDto lot = readLot(exchange);

        if (!isValid(lot)) {
            sendJson(exchange, 400, new ErrorResponse("Donnees lot invalides"));
            return;
        }

        LotProductionDto updated = repository.update(id, lot);
        if (updated == null) {
            sendJson(exchange, 404, new ErrorResponse("Lot non trouve"));
            return;
        }

        sendJson(exchange, 200, updated);
    }

    private void handleDelete(HttpExchange exchange, int id) throws IOException {
        boolean deleted = repository.delete(id);

        if (!deleted) {
            sendJson(exchange, 404, new ErrorResponse("Lot non trouve"));
            return;
        }

        sendJson(exchange, 200, Map.of("success", true));
    }

    private LotProductionDto readLot(HttpExchange exchange) throws IOException {
        Type type = new TypeToken<Map<String, Object>>() {}.getType();
        Map<String, Object> body = gson.fromJson(
                new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8),
                type
        );

        if (body == null) {
            body = new HashMap<>();
        }

        LotProductionDto lot = new LotProductionDto();
        lot.setOperationProcessId(getInteger(body, "operationProcessId"));
        lot.setArticleMatiereId(getInteger(body, "articleMatiereId"));
        lot.setDate(getString(body, "date"));
        lot.setStatutQualite(getString(body, "statutQualite"));
        return lot;
    }

    private boolean isValid(LotProductionDto lot) {
        return lot.getOperationProcessId() != null
                && lot.getOperationProcessId() > 0
                && lot.getArticleMatiereId() != null
                && lot.getArticleMatiereId() > 0
                && !isBlank(lot.getDate())
                && !isBlank(lot.getStatutQualite());
    }

    private Integer getAtelierId(HttpExchange exchange) {
        String query = exchange.getRequestURI().getQuery();

        if (query == null || query.isBlank()) {
            return null;
        }

        String[] params = query.split("&");
        for (String param : params) {
            String[] parts = param.split("=");
            if (parts.length == 2 && "atelierId".equals(parts[0])) {
                return toInteger(parts[1]);
            }
        }

        return null;
    }

    private int getIdFromPath(String path) {
        return Integer.parseInt(path.replace("/api/chef/lots/", ""));
    }

    private String getString(Map<String, Object> body, String key) {
        Object value = body.get(key);

        if (value == null) {
            return "";
        }

        return String.valueOf(value).trim();
    }

    private Integer getInteger(Map<String, Object> body, String key) {
        Object value = body.get(key);

        if (value instanceof Number) {
            return ((Number) value).intValue();
        }

        if (value instanceof String) {
            return toInteger((String) value);
        }

        return null;
    }

    private Integer toInteger(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
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
