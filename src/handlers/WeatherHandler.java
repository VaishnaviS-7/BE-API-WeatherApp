package handlers;

import com.sun.net.httpserver.*;
import models.CityWeather;
import utils.JsonUtil;
import com.google.gson.Gson;

import java.io.*;
import java.util.*;
import java.nio.charset.StandardCharsets;

public class WeatherHandler implements HttpHandler {
    private static final Gson gson = new Gson();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String rawMethod = exchange.getRequestMethod();
        String normalizedMethod = rawMethod.trim().toUpperCase();
        String query = exchange.getRequestURI().getQuery();

        System.out.println("📡 Incoming Request - Method: " + rawMethod + ", URI: " + exchange.getRequestURI());
        System.out.println("➡️ Normalized Method: [" + normalizedMethod + "] (length: " + normalizedMethod.length() + ")");

        switch (normalizedMethod) {
            case "GET":
                if (query != null && query.contains("city=")) {
                    System.out.println("🔍 Routing to handleSearch");
                    handleSearch(exchange);
                } else {
                    System.out.println("📃 Routing to handleGetAll");
                    handleGetAll(exchange);
                }
                break;

            case "POST":
                System.out.println("➕ Routing to handlePost");
                handlePost(exchange);
                break;

            case "PUT":
                System.out.println("✏️ Routing to handlePut");
                handlePut(exchange);
                break;

            case "DELETE":
                System.out.println("🗑️ Routing to handleDelete");
                handleDelete(exchange);
                break;

            case "OPTIONS":
                System.out.println("🧩 Handling OPTIONS preflight");
                Headers headers = exchange.getResponseHeaders();
                headers.set("Access-Control-Allow-Origin", "*");
                headers.set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
                headers.set("Access-Control-Allow-Headers", "Content-Type");
                exchange.sendResponseHeaders(204, -1);
                break;

            default:
                System.out.println("❌ Unknown method: " + normalizedMethod);
                sendResponse(exchange, "Method Not Allowed", 405);
        }
    }

    private void handleGetAll(HttpExchange exchange) throws IOException {
        List<CityWeather> weatherList = JsonUtil.readWeather();
        sendJson(exchange, weatherList, 200);
    }

    private void handleSearch(HttpExchange exchange) throws IOException {
        Map<String, String> params = queryToMap(exchange.getRequestURI().getQuery());
        String city = params.getOrDefault("city", "").toLowerCase();

        List<CityWeather> weatherList = JsonUtil.readWeather();
        for (CityWeather weather : weatherList) {
            if (weather.city.toLowerCase().equals(city)) {
                sendJson(exchange, weather, 200);
                return;
            }
        }
        sendResponse(exchange, "City not found", 404);
    }

    private void handlePost(HttpExchange exchange) throws IOException {
        System.out.println("➡️ [POST] /weather called");

        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        System.out.println("📦 Request Body: " + body);

        CityWeather newWeather = gson.fromJson(body, CityWeather.class);
        System.out.println("✅ Parsed CityWeather: " + newWeather.city + ", " + newWeather.condition + ", " + newWeather.temperature);

        try {
            List<CityWeather> weatherList = JsonUtil.readWeather();

            boolean exists = weatherList.stream()
                    .anyMatch(w -> w.city.equalsIgnoreCase(newWeather.city));

            if (exists) {
                System.out.println("⚠️ City already exists: " + newWeather.city);
                sendResponse(exchange, "City already exists", 409);
                return;
            }

            int newId = weatherList.stream().mapToInt(w -> w.id).max().orElse(0) + 1;
            newWeather.id = newId;

            weatherList.add(newWeather);
            JsonUtil.writeWeather(weatherList);

            System.out.println("📝 New weather added with ID: " + newId);
            sendResponse(exchange, "Weather data added with ID: " + newId, 201);
        } catch (Exception e) {
            System.err.println("❌ Exception in handlePost: " + e.getMessage());
            e.printStackTrace();
            sendResponse(exchange, "Internal Server Error", 500);
        }
    }

    private void handlePut(HttpExchange exchange) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        CityWeather updated = gson.fromJson(body, CityWeather.class);

        List<CityWeather> weatherList = JsonUtil.readWeather();

        boolean duplicateCity = weatherList.stream()
                .anyMatch(w -> w.id != updated.id && w.city.equalsIgnoreCase(updated.city));

        if (duplicateCity) {
            System.out.println("⚠️ Cannot update. Duplicate city name exists: " + updated.city);
            sendResponse(exchange, "City name already exists", 409);
            return;
        }

        boolean found = false;
        for (CityWeather w : weatherList) {
            if (w.id == updated.id) {
                w.city = updated.city;
                w.condition = updated.condition;
                w.temperature = updated.temperature;
                found = true;
                break;
            }
        }

        if (found) {
            JsonUtil.writeWeather(weatherList);
            sendResponse(exchange, "Weather data updated", 200);
        } else {
            sendResponse(exchange, "Weather data not found", 404);
        }
    }

    private void handleDelete(HttpExchange exchange) throws IOException {
        Map<String, String> params = queryToMap(exchange.getRequestURI().getQuery());
        int id = Integer.parseInt(params.getOrDefault("id", "-1"));

        List<CityWeather> weatherList = JsonUtil.readWeather();
        boolean removed = weatherList.removeIf(w -> w.id == id);

        if (removed) {
            JsonUtil.writeWeather(weatherList);
            sendResponse(exchange, "Weather data deleted", 200);
        } else {
            sendResponse(exchange, "Weather entry not found", 404);
        }
    }

    private void sendJson(HttpExchange exchange, Object data, int statusCode) throws IOException {
        String response = gson.toJson(data);
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);

        Headers headers = exchange.getResponseHeaders();
        headers.set("Access-Control-Allow-Origin", "*");
        headers.set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        headers.set("Access-Control-Allow-Headers", "Content-Type");
        headers.set("Content-Type", "application/json");

        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private void sendResponse(HttpExchange exchange, String msg, int code) throws IOException {
        sendJson(exchange, Map.of("message", msg), code);
    }

    private Map<String, String> queryToMap(String query) {
        Map<String, String> map = new HashMap<>();
        if (query == null) return map;
        for (String pair : query.split("&")) {
            String[] parts = pair.split("=");
            if (parts.length == 2) {
                map.put(parts[0], parts[1]);
            }
        }
        return map;
    }
}
