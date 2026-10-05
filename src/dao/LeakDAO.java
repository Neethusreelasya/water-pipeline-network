package dao;

import util.DBConnection;
import java.sql.*;
import java.util.*;

public class LeakDAO {
    public void insertLeak(String areaId, int severity, String description, long timestamp) throws SQLException {
        String sql = "INSERT INTO leaks (area_id, severity, description, reported_timestamp) VALUES (?, ?, ?, ?)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, areaId); ps.setInt(2, severity);
            ps.setString(3, description); ps.setLong(4, timestamp);
            ps.executeUpdate();
        }
    }

    public List<Map<String, Object>> getAllUnresolvedLeaks() throws SQLException {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT * FROM leaks WHERE resolved = FALSE";
        try (Connection c = DBConnection.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("id", rs.getInt("id"));
                row.put("area_id", rs.getString("area_id"));
                row.put("severity", rs.getInt("severity"));
                row.put("description", rs.getString("description"));
                row.put("reported_timestamp", rs.getLong("reported_timestamp"));
                list.add(row);
            }
        }
        return list;
    }
}