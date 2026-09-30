package com.functionhall.dao;
import com.functionhall.model.FoodItem; import com.functionhall.util.DBConnection;
import java.sql.*; import java.util.*;
public class FoodDAO {
    private FoodItem map(ResultSet rs) throws SQLException {
        FoodItem f = new FoodItem(); f.id = rs.getInt("id"); f.itemName = rs.getString("item_name"); f.category = rs.getString("category");
        f.foodType = rs.getString("food_type"); f.price = rs.getDouble("price"); f.available = rs.getBoolean("available"); return f;
    }
    /** type = "Veg", "NonVeg" or null for all */
    public List<FoodItem> find(String type) throws SQLException {
        List<FoodItem> l = new ArrayList<>();
        String sql = "SELECT * FROM food_items WHERE available=TRUE" + (type != null ? " AND food_type=?" : "") + " ORDER BY food_type, id";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            if (type != null) ps.setString(1, type);
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) l.add(map(rs)); }
        }
        return l;
    }
    public boolean add(FoodItem f) throws SQLException {
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement("INSERT INTO food_items(item_name,category,food_type,price,available) VALUES(?,?,?,?,TRUE)")) {
            ps.setString(1, f.itemName); ps.setString(2, f.category); ps.setString(3, f.foodType); ps.setDouble(4, f.price); return ps.executeUpdate() > 0;
        }
    }
    public boolean update(FoodItem f) throws SQLException {
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement("UPDATE food_items SET item_name=?,category=?,food_type=?,price=?,available=? WHERE id=?")) {
            ps.setString(1, f.itemName); ps.setString(2, f.category); ps.setString(3, f.foodType); ps.setDouble(4, f.price); ps.setBoolean(5, f.available); ps.setInt(6, f.id); return ps.executeUpdate() > 0;
        }
    }
    public boolean delete(int id) throws SQLException {
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement("DELETE FROM food_items WHERE id=?")) { ps.setInt(1, id); return ps.executeUpdate() > 0; }
    }
}
