package dao;

import java.sql.*;
import model.Product;
import util.DBConnection;

public class ProductDAO {

    public void createTable() {
        try (
            Connection conn = DBConnection.getConnection();
            Statement st = conn.createStatement()
        ) {
            st.execute("CREATE TABLE IF NOT EXISTS products(id INTEGER PRIMARY KEY AUTOINCREMENT, farmer_id INT, name TEXT, quantity INT, price REAL)");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void add(Product p) {
        String sql = "INSERT INTO products(farmer_id, name, quantity, price) VALUES(?,?,?,?)";

        try (
            Connection conn = util.DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setInt(1, p.getFarmerId());
            ps.setString(2, p.getName());
            ps.setInt(3, p.getQuantity());
            ps.setDouble(4, p.getPrice());
            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void view() {
        try {
            ResultSet rs = DBConnection.getConnection().createStatement()
                    .executeQuery("SELECT * FROM products");

            while (rs.next()) {
                System.out.println(rs.getInt("id") + " " +
                        rs.getString("name") +
                        " Qty:" + rs.getInt("quantity") +
                        " Price:" + rs.getDouble("price"));
            }
        } catch (Exception e) { e.printStackTrace(); }
    }
}