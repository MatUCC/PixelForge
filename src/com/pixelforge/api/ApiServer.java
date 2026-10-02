package com.pixelforge.api;

import com.pixelforge.service.ImageProcessingService;
import com.pixelforge.service.ImageProcessingService.ProcessingResult;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.Executors;

/**
 * REST API for the frontend, using the HTTP server included in the JDK (no frameworks).
 *
 *   GET  /api/health                           -> "ok" (used by the hosting platform)
 *   GET  /api/treatments                       -> catalog of treatments (to build the menu)
 *   POST /api/process?treatments=A,B:param,... -> body = raw image bytes; returns order + final image in Base64
 *   GET  /api/orders                           -> order history
 *   GET  /                                     -> the frontend (static files from the "frontend" folder)
 *
 * Configuration through environment variables (all optional):
 *   PORT            port to listen on (default 8080; Render/Railway set it automatically)
 *   FRONTEND_DIR    folder with index.html (default "frontend")
 *   ALLOWED_ORIGIN  value for Access-Control-Allow-Origin (default "*")
 */
public class ApiServer {

    private static final Map<String, String> CONTENT_TYPES = Map.of(
            "html", "text/html; charset=utf-8",
            "css", "text/css; charset=utf-8",
            "js", "text/javascript; charset=utf-8",
            "json", "application/json; charset=utf-8",
            "png", "image/png",
            "jpg", "image/jpeg",
            "svg", "image/svg+xml",
            "ico", "image/x-icon");

    private final ImageProcessingService service = new ImageProcessingService();
    private final Path frontendDir;
    private final String allowedOrigin;

    public ApiServer(Path frontendDir, String allowedOrigin) {
        this.frontendDir = frontendDir.toAbsolutePath().normalize();
        this.allowedOrigin = allowedOrigin;
    }

    public static void main(String[] args) throws IOException {
        System.setProperty("java.awt.headless", "true"); // servers have no screen
        int port = Integer.parseInt(env("PORT", "8080"));
        new ApiServer(Paths.get(env("FRONTEND_DIR", "frontend")), env("ALLOWED_ORIGIN", "*")).start(port);
    }

    public void start(int port) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/api/health", this::handleHealth);
        server.createContext("/api/treatments", this::handleTreatments);
        server.createContext("/api/process", this::handleProcess);
        server.createContext("/api/orders", this::handleOrders);
        server.createContext("/", this::handleStatic);
        server.setExecutor(Executors.newFixedThreadPool(8)); // the default executor handles one request at a time
        server.start();
        System.out.println("PixelForge running on http://localhost:" + port + "  (frontend: " + frontendDir + ")");
    }

    private static String env(String name, String defaultValue) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? defaultValue : value;
    }

    private void handleHealth(HttpExchange exchange) throws IOException {
        send(exchange, 200, "{\"status\":\"ok\"}");
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
            byte[] imageBytes = readBody(exchange, ImageProcessingService.MAX_IMAGE_BYTES);
            String treatments = getQueryParam(exchange, "treatments");

            ProcessingResult result = service.process(imageBytes, treatments);

            String base64 = Base64.getEncoder().encodeToString(result.getImageBytes());
            send(exchange, 200, Json.processResult(result.getOrder(), result.getFormat(), base64));
        } catch (IllegalArgumentException e) {
            send(exchange, 400, Json.error(e.getMessage())); // business rule or bad input
        } catch (IOException e) {
            send(exchange, 400, Json.error("The image could not be read (only JPG or PNG are supported)"));
        } catch (RuntimeException e) {
            send(exchange, 500, Json.error("Unexpected error: " + e.getMessage()));
        }
    }

    /** Serves the frontend. Unknown paths fall back to index.html. */
    private void handleStatic(HttpExchange exchange) throws IOException {
        if (!requireMethod(exchange, "GET")) {
            return;
        }
        String requested = URLDecoder.decode(exchange.getRequestURI().getPath(), StandardCharsets.UTF_8);
        Path file = frontendDir.resolve(requested.substring(1)).normalize();
        if (!file.startsWith(frontendDir)) { // blocks "../" tricks
            send(exchange, 404, Json.error("Not found"));
            return;
        }
        if (Files.isDirectory(file)) {
            file = file.resolve("index.html");
        }
        if (!Files.isRegularFile(file)) {
            file = frontendDir.resolve("index.html");
            if (!Files.isRegularFile(file)) {
                send(exchange, 404, Json.error("Frontend not found in " + frontendDir));
                return;
            }
        }
        String name = file.getFileName().toString();
        String extension = name.substring(name.lastIndexOf('.') + 1).toLowerCase();
        byte[] body = Files.readAllBytes(file);
        exchange.getResponseHeaders().add("Content-Type", CONTENT_TYPES.getOrDefault(extension, "application/octet-stream"));
        exchange.sendResponseHeaders(200, body.length);
        try (OutputStream output = exchange.getResponseBody()) {
            output.write(body);
        }
    }

    // ---------- small HTTP helpers ----------

    /** Reads at most maxBytes + 1, so a huge upload is rejected without loading it all into memory. */
    private byte[] readBody(HttpExchange exchange, int maxBytes) throws IOException {
        try (InputStream input = exchange.getRequestBody()) {
            byte[] body = input.readNBytes(maxBytes + 1);
            if (body.length > maxBytes) {
                throw new IllegalArgumentException("The image must be between 1 byte and 5 MB");
            }
            return body;
        }
    }

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
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", allowedOrigin);
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
