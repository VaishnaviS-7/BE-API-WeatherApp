package utils;

import models.User;
import models.CityWeather;

import java.io.*;
import java.util.*;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

public class JsonUtil {
    private static final String USER_FILE = "data/users.json";
    private static final String WEATHER_FILE = "data/weather.json";

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    // === User methods ===
    public static List<User> readUsers() {
        try (Reader reader = new FileReader(USER_FILE)) {
            List<User> list = gson.fromJson(reader, new TypeToken<List<User>>() {}.getType());
            return list != null ? list : new ArrayList<>();
        } catch (Exception e) {
            System.err.println("❌ Failed to read user JSON: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public static void writeUsers(List<User> users) {
        try (Writer writer = new FileWriter(USER_FILE)) {
            gson.toJson(users, writer);
        } catch (IOException e) {
            System.err.println("❌ Failed to write to user JSON: " + e.getMessage());
        }
    }

    // === Weather methods ===
    public static List<CityWeather> readWeather() {
        try (Reader reader = new FileReader(WEATHER_FILE)) {
            List<CityWeather> list = gson.fromJson(reader, new TypeToken<List<CityWeather>>() {}.getType());
            return list != null ? list : new ArrayList<>();
        } catch (Exception e) {
            System.err.println("❌ Failed to read weather JSON: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public static void writeWeather(List<CityWeather> weatherList) {
        try (Writer writer = new FileWriter(WEATHER_FILE)) {
            gson.toJson(weatherList, writer);
        } catch (IOException e) {
            System.err.println("❌ Failed to write to weather JSON: " + e.getMessage());
        }
    }
}
