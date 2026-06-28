package org.example.controller;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.example.dto.AnalyseLaboratoireDto;
import org.example.dto.ErrorResponse;
import org.example.dto.OperationProcessDto;
import org.example.repository.LaboAnalyseRepository;
import org.example.repository.OperationProcessRepository;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TechnicienLaboController implements HttpHandler {

    private final Gson gson = new Gson();
    private final OperationProcessRepository operationRepository = new OperationProcessRepository();
    private final LaboAnalyseRepository analyseRepository = new LaboAnalyseRepository();

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

            if ("GET".equalsIgnoreCase(method) && "/api/labo/operations".equals(path)) {
                handleOperations(exchange);
                return;
            }

            if ("GET".equalsIgnoreCase(method) && "/api/labo/analyses".equals(path)) {
                handleAnalyses(exchange);
                return;
            }

            if ("GET".equalsIgnoreCase(method) && path.endsWith("/details")) {
                int id = getOperationDetailsId(path);
                Object details = operationRepository.findDetailsById(id);

                if (details == null) {
                    sendJson(exchange, 404, new ErrorResponse("Details operation introuvables"));
                    return;
                }

                sendJson(exchange, 200, details);
                return;
            }

            if ("POST".equalsIgnoreCase(method) && "/api/labo/analyses".equals(path)) {
                handleSaveAnalyse(exchange);
                return;
            }

            sendJson(exchange, 404, new ErrorResponse("Route labo introuvable"));
        } catch (Exception exception) {
            exception.printStackTrace();
            sendJson(exchange, 500, new ErrorResponse("Erreur serveur : " + exception.getMessage()));
        }
    }

    private void handleOperations(HttpExchange exchange) throws IOException {
        Integer atelierId = getAtelierId(exchange);
        List<OperationProcessDto> operations = operationRepository.findByAtelierId(atelierId);

        Map<String, Object> response = new HashMap<>();
        response.put("operations", operations);
        sendJson(exchange, 200, response);
    }

    private void handleAnalyses(HttpExchange exchange) throws IOException {
        Integer atelierId = getAtelierId(exchange);

        if (atelierId == null || atelierId <= 0) {
            sendJson(exchange, 400, new ErrorResponse("Atelier manquant"));
            return;
        }

        Map<String, Object> response = new HashMap<>();
        response.put("analyses", analyseRepository.findByAtelier(atelierId));
        sendJson(exchange, 200, response);
    }

    private void handleSaveAnalyse(HttpExchange exchange) throws IOException {
        Type type = new TypeToken<Map<String, Object>>() {}.getType();
        Map<String, Object> body = gson.fromJson(
                new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8),
                type
        );

        if (body == null) {
            body = new HashMap<>();
        }

        int atelierId = safeInt(body.get("atelierId"));
        AnalyseLaboratoireDto analyse = new AnalyseLaboratoireDto();
        analyse.setLotProductionId(toInteger(body.get("lotProductionId")));
        analyse.setTauxP2O5(safeFloat(body.get("tauxP2O5")));
        analyse.setTauxCadmiumPpm(safeFloat(body.get("tauxCadmiumPpm")));
        analyse.setSolidesSuspendu(safeFloat(body.get("solidesSuspendu")));
        analyse.setDateAnalyse(safeString(body.get("dateAnalyse")));

        if (!isValid(analyse, atelierId)) {
            sendJson(exchange, 400, new ErrorResponse("Donnees analyse invalides"));
            return;
        }

        AnalyseLaboratoireDto saved = analyseRepository.saveForAtelier(analyse, atelierId);
        if (saved == null) {
            sendJson(exchange, 403, new ErrorResponse("Lot introuvable pour cet atelier"));
            return;
        }

        sendJson(exchange, 200, saved);
    }

    private boolean isValid(AnalyseLaboratoireDto analyse, int atelierId) {
        return atelierId > 0
                && analyse.getLotProductionId() != null
                && analyse.getLotProductionId() > 0
                && analyse.getTauxP2O5() >= 0
                && analyse.getTauxCadmiumPpm() >= 0
                && analyse.getSolidesSuspendu() >= 0
                && analyse.getDateAnalyse() != null
                && !analyse.getDateAnalyse().isBlank();
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

    private int getOperationDetailsId(String path) {
        String idText = path
                .replace("/api/labo/operations/", "")
                .replace("/details", "");
        return Integer.parseInt(idText);
    }

    private Integer toInteger(Object value) {
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }

        if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException exception) {
                return null;
            }
        }

        return null;
    }

    private int safeInt(Object value) {
        Integer number = toInteger(value);
        return number == null ? 0 : number;
    }

    private float safeFloat(Object value) {
        if (value instanceof Number) {
            return ((Number) value).floatValue();
        }

        if (value instanceof String) {
            try {
                return Float.parseFloat((String) value);
            } catch (NumberFormatException exception) {
                return 0;
            }
        }

        return 0;
    }

    private String safeString(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }

    private void addCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
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
