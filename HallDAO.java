package com.functionhall.dao;
import com.functionhall.model.Hall;
import com.functionhall.util.DBConnection;
import java.sql.*; import java.util.*;
public class HallDAO {
    private Hall map(ResultSet rs) throws SQLException {
        Hall h = new Hall(); h.id = rs.getInt("id"); h.hallName = rs.getString("hall_name"); h.capacity = rs.getInt("capacity");
        h.price = rs.getDouble("price"); h.ac = rs.getBoolean("ac"); h.parking = rs.getBoolean("parking");
        h.stage = rs.getBoolean("stage"); h.status = rs.getString("status"); return h;
    }
    public List<Hall> findAll() throws SQLException {                       // READ
        List<Hall> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement("SELECT * FROM halls");
             ResultSet rs = ps.executeQuery()) { while (rs.next()) list.add(map(rs)); }
        return list;
    }
    public Hall findById(int id) throws SQLException {
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement("SELECT * FROM halls WHERE id=?")) {
            ps.setInt(1, id); try (ResultSet rs = ps.executeQuery()) { return rs.next() ? map(rs) : null; }
        }
    }
    public boolean add(Hall h) throws SQLException {                        // CREATE
        String sql = "INSERT INTO halls(hall_name,capacity,price,ac,parking,stage,status) VALUES(?,?,?,?,?,?,?)";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, h.hallName); ps.setInt(2, h.capacity); ps.setDouble(3, h.price); ps.setBoolean(4, h.ac);
            ps.setBoolean(5, h.parking); ps.setBoolean(6, h.stage); ps.setString(7, h.status); return ps.executeUpdate() > 0;
        }
    }
    public boolean update(Hall h) throws SQLException {                     // UPDATE
        String sql = "UPDATE halls SET hall_name=?,capacity=?,price=?,ac=?,parking=?,stage=?,status=? WHERE id=?";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, h.hallName); ps.setInt(2, h.capacity); ps.setDouble(3, h.price); ps.setBoolean(4, h.ac);
            ps.setBoolean(5, h.parking); ps.setBoolean(6, h.stage); ps.setString(7, h.status); ps.setInt(8, h.id); return ps.executeUpdate() > 0;
        }
    }
    public boolean delete(int id) throws SQLException {                     // DELETE
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement("DELETE FROM halls WHERE id=?")) {
            ps.setInt(1, id); return ps.executeUpdate() > 0;
        }
    }
}
