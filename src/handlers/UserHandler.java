package handlers;

import com.sun.net.httpserver.*;
import models.User;
import utils.JsonUtil;
import com.google.gson.Gson;

import java.io.*;
import java.util.*;
import java.nio.charset.StandardCharsets;

public class UserHandler implements HttpHandler {
    private static final Gson gson = new Gson();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        switch (method) {
            case "GET":
                handleGet(exchange);
                break;
            case "POST":
                handlePost(exchange);
                break;
            case "PUT":
                handlePut(exchange);
                break;
            case "DELETE":
                handleDelete(exchange);
                break;
            default:
                sendResponse(exchange, "Method Not Allowed", 405);
        }
    }

    private void handleGet(HttpExchange exchange) throws IOException {
        List<User> users = JsonUtil.readUsers();
        String response = gson.toJson(users);
        sendResponse(exchange, response, 200);
    }

    private void handlePost(HttpExchange exchange) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        User newUser = gson.fromJson(body, User.class);

        List<User> users = JsonUtil.readUsers();
        int newId = users.stream().mapToInt(u -> u.id).max().orElse(0) + 1;
        newUser.id = newId;
        users.add(newUser);
        JsonUtil.writeUsers(users);

        sendResponse(exchange, "User created with ID: " + newUser.id, 201);
    }

    private void handlePut(HttpExchange exchange) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        User updatedUser = gson.fromJson(body, User.class);

        List<User> users = JsonUtil.readUsers();
        boolean found = false;
        for (User user : users) {
            if (user.id == updatedUser.id) {
                user.name = updatedUser.name;
                user.email = updatedUser.email;
                found = true;
                break;
            }
        }

        if (found) {
            JsonUtil.writeUsers(users);
            sendResponse(exchange, "User updated", 200);
        } else {
            sendResponse(exchange, "User not found", 404);
        }
    }

    private void handleDelete(HttpExchange exchange) throws IOException {
        String query = exchange.getRequestURI().getQuery();
        Map<String, String> params = queryToMap(query);
        int idToDelete = Integer.parseInt(params.getOrDefault("id", "-1"));

        List<User> users = JsonUtil.readUsers();
        boolean removed = users.removeIf(user -> user.id == idToDelete);

        if (removed) {
            JsonUtil.writeUsers(users);
            sendResponse(exchange, "User deleted", 200);
        } else {
            sendResponse(exchange, "User not found", 404);
        }
    }

    private void sendResponse(HttpExchange exchange, String response, int statusCode) throws IOException {
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
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
