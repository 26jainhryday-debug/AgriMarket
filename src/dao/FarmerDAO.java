package dao;

import java.sql.*;
import model.Farmer;
import util.DBConnection;

public class FarmerDAO {

    public void createTable() {
        try (
            Connection conn = DBConnection.getConnection();
            Statement st = conn.createStatement()
        ) {
            st.execute("CREATE TABLE IF NOT EXISTS farmers(id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, age INT, location TEXT)");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void add(Farmer f) {
        String sql = "INSERT INTO farmers(name, age, location) VALUES(?,?,?)";

        try (
            Connection conn = util.DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setString(1, f.getName());
            ps.setInt(2, f.getAge());
            ps.setString(3, f.getLocation());
            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void view() {
        try {
            ResultSet rs = DBConnection.getConnection().createStatement()
                    .executeQuery("SELECT * FROM farmers");

            while (rs.next()) {
                System.out.println(rs.getInt("id") + " " +
                        rs.getString("name") + " " +
                        rs.getInt("age") + " " +
                        rs.getString("location"));
            }
        } catch (Exception e) { e.printStackTrace(); }
    }
}