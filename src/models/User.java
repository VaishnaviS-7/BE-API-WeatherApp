package models;

public class User {
    public int id;
    public String name;
    public String email;

    public User() {}

    public User(int id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }
}
