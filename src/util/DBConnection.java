package util;

import java.sql.Connection;
import java.sql.DriverManager;

public class DBConnection {
    public static Connection getConnection() {
        try {
            Class.forName("org.sqlite.JDBC");  // MUST be here
            return DriverManager.getConnection("jdbc:sqlite:agri.db?busy_timeout=5000");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}