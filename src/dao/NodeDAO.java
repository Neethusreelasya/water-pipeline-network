package dao;

import util.DBConnection;
import java.sql.*;
import java.util.*;

public class NodeDAO {
    public void insertNode(String id, String name, String type, double val1, double val2) throws SQLException {
        String sql = "INSERT INTO nodes (id, name, type, val1, val2) VALUES (?, ?, ?, ?, ?)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, id); ps.setString(2, name); ps.setString(3, type);
            ps.setDouble(4, val1); ps.setDouble(5, val2);
            ps.executeUpdate();
        }
    }

    public List<Map<String, Object>> getAllNodes() throws SQLException {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT * FROM nodes";
        try (Connection c = DBConnection.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("id", rs.getString("id"));
                row.put("name", rs.getString("name"));
                row.put("type", rs.getString("type"));
                row.put("val1", rs.getDouble("val1"));
                row.put("val2", rs.getDouble("val2"));
                list.add(row);
            }
        }
        return list;
    }
}