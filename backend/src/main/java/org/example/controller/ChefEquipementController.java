package org.example.controller;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.example.dto.ErrorResponse;
import org.example.model.Equipement;
import org.example.repository.EquipementRepository;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChefEquipementController implements HttpHandler {

    private static final Gson gson = new Gson();
    private final EquipementRepository repository = new EquipementRepository();

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
            String[] parts = path.split("/");

            if ("GET".equalsIgnoreCase(method)) {
                handleGet(exchange);
                return;
            }

            if ("POST".equalsIgnoreCase(method)) {
                handlePost(exchange);
                return;
            }

            if ("PUT".equalsIgnoreCase(method) || "DELETE".equalsIgnoreCase(method)) {
                if (parts.length >= 5) {
                    int id = Integer.parseInt(parts[4]);
                    if ("PUT".equalsIgnoreCase(method)) {
                        handlePut(exchange, id);
                        return;
                    } else {
                        handleDelete(exchange, id);
                        return;
                    }
                } else {
                    sendJson(exchange, 400, new ErrorResponse("ID manquant"));
                    return;
                }
            }

            sendJson(exchange, 405, new ErrorResponse("Methode non autorisee"));
        } catch (Exception ex) {
            sendJson(exchange, 500, new ErrorResponse(ex.getMessage()));
        }
    }

    private void handleGet(HttpExchange exchange) throws IOException {
        String query = exchange.getRequestURI().getQuery();
        Integer atelierId = null;

        if (query != null && !query.isBlank()) {
            String[] params = query.split("&");
            for (String p : params) {
                String[] kv = p.split("=");
                if (kv.length == 2 && "atelierId".equals(kv[0])) {
                    try { atelierId = Integer.parseInt(kv[1]); } catch (NumberFormatException ignored) {}
                }
            }
        }

        List<Equipement> list = repository.findByAtelierId(atelierId);
        Map<String, Object> resp = new HashMap<>();
        resp.put("equipements", list);
        sendJson(exchange, 200, resp);
    }

    private void handlePost(HttpExchange exchange) throws IOException {
        Type type = new TypeToken<Map<String, Object>>(){}.getType();
        Map<String, Object> body = gson.fromJson(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8), type);
        if (body == null) body = new HashMap<>();

        Equipement e = new Equipement();
        e.setTagIndustriel((String) body.getOrDefault("tagIndustriel", ""));
        e.setTypeEquipement((String) body.getOrDefault("typeEquipement", ""));
        e.setNomEquipement((String) body.getOrDefault("nomEquipement", ""));
        e.setStatutEquipement((String) body.getOrDefault("statutEquipement", "OPERATIONNEL"));

        Integer atelierId = null;
        Object aobj = body.get("atelierId");
        if (aobj instanceof Number) atelierId = ((Number) aobj).intValue();
        else if (aobj instanceof String) {
            try { atelierId = Integer.parseInt((String) aobj); } catch (NumberFormatException ignored) {}
        }

        Equipement created = repository.create(e, atelierId == null ? 0 : atelierId);
        if (created == null) {
            sendJson(exchange, 500, new ErrorResponse("Impossible de creer equipement"));
            return;
        }

        sendJson(exchange, 201, created);
    }

    private void handlePut(HttpExchange exchange, int id) throws IOException {
        Type type = new TypeToken<Map<String, Object>>(){}.getType();
        Map<String, Object> body = gson.fromJson(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8), type);
        if (body == null) body = new HashMap<>();

        Equipement e = new Equipement();
        e.setTagIndustriel((String) body.getOrDefault("tagIndustriel", ""));
        e.setTypeEquipement((String) body.getOrDefault("typeEquipement", ""));
        e.setNomEquipement((String) body.getOrDefault("nomEquipement", ""));
        e.setStatutEquipement((String) body.getOrDefault("statutEquipement", "OPERATIONNEL"));

        Equipement updated = repository.update(id, e);
        if (updated == null) {
            sendJson(exchange, 404, new ErrorResponse("Equipement non trouve"));
            return;
        }

        sendJson(exchange, 200, updated);
    }

    private void handleDelete(HttpExchange exchange, int id) throws IOException {
        boolean ok = repository.delete(id);
        if (!ok) {
            sendJson(exchange, 404, new ErrorResponse("Equipement non trouve"));
            return;
        }

        sendJson(exchange, 200, Map.of("success", true));
    }

    private void addCorsHeaders(HttpExchange exchange) {
        // Allow all origins for local development; adjust for production
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
