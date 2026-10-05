package dao;

import util.DBConnection;
import java.sql.*;
import java.util.*;

public class PipelineDAO {
    public void insertPipeline(String fromId, String toId, double weight) throws SQLException {
        String sql = "INSERT INTO pipelines (from_id, to_id, weight) VALUES (?, ?, ?)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, fromId); ps.setString(2, toId); ps.setDouble(3, weight);
            ps.executeUpdate();
        }
    }

    public List<Map<String, Object>> getAllPipelines() throws SQLException {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT * FROM pipelines WHERE is_active = TRUE";
        try (Connection c = DBConnection.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("from_id", rs.getString("from_id"));
                row.put("to_id", rs.getString("to_id"));
                row.put("weight", rs.getDouble("weight"));
                list.add(row);
            }
        }
        return list;
    }
}