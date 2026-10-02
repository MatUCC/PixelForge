package com.pixelforge.api;

import com.pixelforge.service.ImageProcessingService;
import com.pixelforge.service.ImageProcessingService.ProcessingResult;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * REST API for the frontend, using the HTTP server included in the JDK (no frameworks).
 *
 *   GET  /api/treatments                       -> catalog of treatments (to build the menu)
 *   POST /api/process?treatments=A,B:param,... -> body = raw image bytes; returns order + final image in Base64
 *   GET  /api/orders                           -> order history
 *
 * CORS is enabled so a frontend served from another port/folder can call it.
 */
public class ApiServer {

    private static final int PORT = 8080;

    private final ImageProcessingService service = new ImageProcessingService();

    public static void main(String[] args) throws IOException {
        new ApiServer().start(PORT);
    }

    public void start(int port) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/api/treatments", this::handleTreatments);
        server.createContext("/api/process", this::handleProcess);
        server.createContext("/api/orders", this::handleOrders);
        server.start();
        System.out.println("PixelForge API running on http://localhost:" + port + "/api/treatments");
    }

    private void handleTreatments(HttpExchange exchange) throws IOException {
        if (handlePreflight(exchange) || !requireMethod(exchange, "GET")) {
            return;
        }
        send(exchange, 200, Json.treatments());
    }

    private void handleOrders(HttpExchange exchange) throws IOException {
        if (handlePreflight(exchange) || !requireMethod(exchange, "GET")) {
            return;
        }
        send(exchange, 200, Json.orders(service.getOrders()));
    }

    private void handleProcess(HttpExchange exchange) throws IOException {
        if (handlePreflight(exchange) || !requireMethod(exchange, "POST")) {
            return;
        }
        try {
            byte[] imageBytes = exchange.getRequestBody().readAllBytes();
            String treatments = getQueryParam(exchange, "treatments");

            ProcessingResult result = service.process(imageBytes, treatments);

            String base64 = Base64.getEncoder().encodeToString(result.getImageBytes());
            send(exchange, 200, Json.processResult(result.getOrder(), result.getFormat(), base64));
        } catch (IllegalArgumentException e) {
            send(exchange, 400, Json.error(e.getMessage())); // business rule or bad input
        } catch (RuntimeException e) {
            send(exchange, 500, Json.error("Unexpected error: " + e.getMessage()));
        }
    }

    // ---------- small HTTP helpers ----------

    private boolean handlePreflight(HttpExchange exchange) throws IOException {
        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            addCorsHeaders(exchange);
            exchange.sendResponseHeaders(204, -1);
            exchange.close();
            return true;
        }
        return false;
    }

    private boolean requireMethod(HttpExchange exchange, String method) throws IOException {
        if (!method.equals(exchange.getRequestMethod())) {
            send(exchange, 405, Json.error("Use " + method));
            return false;
        }
        return true;
    }

    private String getQueryParam(HttpExchange exchange, String name) {
        String query = exchange.getRequestURI().getRawQuery();
        if (query == null) {
            return null;
        }
        for (String pair : query.split("&")) {
            String[] keyValue = pair.split("=", 2);
            if (keyValue[0].equals(name) && keyValue.length == 2) {
                return URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8);
            }
        }
        return null;
    }

    private void addCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
    }

    private void send(HttpExchange exchange, int status, String json) throws IOException {
        byte[] body = json.getBytes(StandardCharsets.UTF_8);
        addCorsHeaders(exchange);
        exchange.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(status, body.length);
        try (OutputStream output = exchange.getResponseBody()) {
            output.write(body);
        }
    }
}
