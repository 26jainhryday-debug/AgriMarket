package dao;

import java.sql.*;
import util.DBConnection;

public class UserDAO {

    public void createTable() {
        try (
            Connection conn = DBConnection.getConnection();
            Statement st = conn.createStatement()
        ) {
            st.execute("CREATE TABLE IF NOT EXISTS users(username TEXT PRIMARY KEY, password TEXT, role TEXT)");
            st.execute("INSERT OR IGNORE INTO users VALUES('admin','admin123','ADMIN')");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String login(String u, String p) {
        try (
            Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(
                "SELECT role FROM users WHERE username=? AND password=?"
            )
        ) {
            ps.setString(1, u);
            ps.setString(2, p);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString("role");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}