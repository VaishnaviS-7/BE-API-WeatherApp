package models;

public class CityWeather {
    public int id;
    public String city;
    public String condition;
    public double temperature;

    public CityWeather() {}

    public CityWeather(int id, String city, String condition, double temperature) {
        this.id = id;
        this.city = city;
        this.condition = condition;
        this.temperature = temperature;
    }
}
