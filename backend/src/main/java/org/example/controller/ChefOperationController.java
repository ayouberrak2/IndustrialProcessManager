package org.example.controller;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.example.dto.ErrorResponse;
import org.example.dto.FluxMatiereDto;
import org.example.dto.OperationProcessDto;
import org.example.repository.OperationProcessRepository;

public class ChefOperationController implements HttpHandler {

    private final Gson gson = new Gson();
    private final OperationProcessRepository repository = new OperationProcessRepository();

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

            if ("GET".equalsIgnoreCase(method) && "/api/chef/operations".equals(path)) {
                handleGet(exchange);
                return;
            }

            if ("GET".equalsIgnoreCase(method) && path.endsWith("/details")) {
                int id = getDetailsIdFromPath(path);
                handleDetails(exchange, id);
                return;
            }

            if ("POST".equalsIgnoreCase(method) && "/api/chef/operations".equals(path)) {
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

            sendJson(exchange, 404, new ErrorResponse("Route operations introuvable"));
        } catch (Exception exception) {
            exception.printStackTrace();
            sendJson(exchange, 500, new ErrorResponse("Erreur serveur : " + exception.getMessage()));
        }
    }

    private void handleGet(HttpExchange exchange) throws IOException {
        Integer atelierId = getAtelierId(exchange);
        List<OperationProcessDto> operations = repository.findByAtelierId(atelierId);

        Map<String, Object> response = new HashMap<>();
        response.put("operations", operations);
        sendJson(exchange, 200, response);
    }

    private void handleDetails(HttpExchange exchange, int id) throws IOException {
        Object details = repository.findDetailsById(id);

        if (details == null) {
            sendJson(exchange, 404, new ErrorResponse("Details operation introuvables"));
            return;
        }

        sendJson(exchange, 200, details);
    }

    private void handlePost(HttpExchange exchange) throws IOException {
        OperationProcessDto operation = readOperation(exchange);

        if (!isValid(operation)) {
            sendJson(exchange, 400, new ErrorResponse("Donnees operation invalides"));
            return;
        }

        OperationProcessDto created = repository.create(operation);
        if (created == null) {
            sendJson(exchange, 400, new ErrorResponse("Impossible de creer operation"));
            return;
        }

        sendJson(exchange, 201, created);
    }

    private void handlePut(HttpExchange exchange, int id) throws IOException {
        OperationProcessDto operation = readOperation(exchange);

        if (!isValid(operation)) {
            sendJson(exchange, 400, new ErrorResponse("Donnees operation invalides"));
            return;
        }

        OperationProcessDto updated = repository.update(id, operation);
        if (updated == null) {
            sendJson(exchange, 404, new ErrorResponse("Operation non trouvee"));
            return;
        }

        sendJson(exchange, 200, updated);
    }

    private void handleDelete(HttpExchange exchange, int id) throws IOException {
        boolean deleted = repository.delete(id);

        if (!deleted) {
            sendJson(exchange, 404, new ErrorResponse("Operation non trouvee"));
            return;
        }

        sendJson(exchange, 200, Map.of("success", true));
    }

    private OperationProcessDto readOperation(HttpExchange exchange) throws IOException {
        Type type = new TypeToken<Map<String, Object>>() {}.getType();
        Map<String, Object> body = gson.fromJson(
                new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8),
                type
        );

        if (body == null) {
            body = new HashMap<>();
        }

        OperationProcessDto operation = new OperationProcessDto();
        operation.setNumOrdreFab(getString(body, "numOrdreFab"));
        operation.setTypeOperation(getString(body, "typeOperation"));
        operation.setStatutOperation(getString(body, "statutOperation"));
        operation.setDateDebut(getString(body, "dateDebut"));
        operation.setDateFin(getString(body, "dateFin"));
        operation.setOperateur(getString(body, "operateur"));
        operation.setEquipementId(getInteger(body, "equipementId"));
        operation.setDureeEstimee(getInteger(body, "dureeEstimee"));
        operation.setEntreeArticleMatiereId(getInteger(body, "entreeArticleMatiereId"));
        operation.setEntreeMesureCapteur(getFloat(body, "entreeMesureCapteur"));
        operation.setEntreeMesureDiametre(getFloat(body, "entreeMesureDiametre"));
        operation.setEntreeFlux(getFluxList(body, "entreeFlux"));
        operation.setSortieArticleMatiereId(getInteger(body, "sortieArticleMatiereId"));
        operation.setSortieMesureCapteur(getFloat(body, "sortieMesureCapteur"));
        operation.setSortieMesureDiametre(getFloat(body, "sortieMesureDiametre"));
        return operation;
    }

    private boolean isValid(OperationProcessDto operation) {
        boolean hasValidEntreeFlux = hasValidEntreeFlux(operation);

        boolean mainFieldsValid = !isBlank(operation.getNumOrdreFab())
                && !isBlank(operation.getTypeOperation())
                && !isBlank(operation.getStatutOperation())
                && !isBlank(operation.getDateDebut())
                && !isBlank(operation.getOperateur())
                && operation.getEquipementId() != null
                && operation.getEquipementId() > 0
                && hasValidEntreeFlux;

        if (!mainFieldsValid) {
            return false;
        }

        if (!isClosingOperation(operation)) {
            return true;
        }

        return operation.getSortieArticleMatiereId() != null
                && operation.getSortieArticleMatiereId() > 0
                && operation.getSortieMesureCapteur() != null
                && operation.getSortieMesureCapteur() >= 0
                && operation.getSortieMesureDiametre() != null
                && !isBlank(operation.getDateFin());
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
        return Integer.parseInt(path.replace("/api/chef/operations/", ""));
    }

    private int getDetailsIdFromPath(String path) {
        String idText = path
                .replace("/api/chef/operations/", "")
                .replace("/details", "");
        return Integer.parseInt(idText);
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

    private Float getFloat(Map<String, Object> body, String key) {
        Object value = body.get(key);

        if (value instanceof Number) {
            return ((Number) value).floatValue();
        }

        if (value instanceof String) {
            try {
                return Float.parseFloat((String) value);
            } catch (NumberFormatException exception) {
                return null;
            }
        }

        return null;
    }

    private List<FluxMatiereDto> getFluxList(Map<String, Object> body, String key) {
        List<FluxMatiereDto> fluxList = new ArrayList<>();
        Object rawValue = body.get(key);

        if (!(rawValue instanceof List<?>)) {
            return fluxList;
        }

        for (Object item : (List<?>) rawValue) {
            if (!(item instanceof Map<?, ?>)) {
                continue;
            }

            Map<?, ?> row = (Map<?, ?>) item;
            FluxMatiereDto flux = new FluxMatiereDto();
            flux.setArticleMatiereId(toInteger(row.get("articleMatiereId")));
            flux.setMesureCapteur(toFloat(row.get("mesureCapteur")));
            flux.setMesureDiametre(toFloat(row.get("mesureDiametre")));
            fluxList.add(flux);
        }

        return fluxList;
    }

    private boolean hasValidEntreeFlux(OperationProcessDto operation) {
        if (operation.getEntreeFlux() != null && !operation.getEntreeFlux().isEmpty()) {
            for (FluxMatiereDto flux : operation.getEntreeFlux()) {
                if (flux.getArticleMatiereId() == null
                        || flux.getArticleMatiereId() <= 0
                        || flux.getMesureCapteur() <= 0) {
                    return false;
                }
            }

            return true;
        }

        return operation.getEntreeArticleMatiereId() != null
                && operation.getEntreeArticleMatiereId() > 0
                && operation.getEntreeMesureCapteur() != null
                && operation.getEntreeMesureCapteur() > 0
                && operation.getEntreeMesureDiametre() != null;
    }

    private Integer toInteger(Object value) {
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }

        if (value instanceof String) {
            return toInteger((String) value);
        }

        return null;
    }

    private float toFloat(Object value) {
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

    private boolean isClosingOperation(OperationProcessDto operation) {
        return !isBlank(operation.getDateFin()) || "TERMINEE".equals(operation.getStatutOperation());
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
