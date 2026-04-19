package model;

public class User {
    private String username;
    private String password;
    private String role;

    public User(String u, String p, String r) {
        username = u;
        password = p;
        role = r;
    }

    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getRole() { return role; }
}