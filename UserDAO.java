package com.functionhall.dao;
import com.functionhall.model.User; import com.functionhall.util.DBConnection;
import java.security.MessageDigest; import java.sql.*; import java.util.*;
public class UserDAO {
    public static String hash(String p) {
        try { StringBuilder sb = new StringBuilder(); for (byte b : MessageDigest.getInstance("SHA-256").digest(p.getBytes("UTF-8"))) sb.append(String.format("%02x", b)); return sb.toString(); }
        catch (Exception e) { throw new RuntimeException(e); }
    }
    private User map(ResultSet rs) throws SQLException { User u = new User(); u.id = rs.getInt("id"); u.name = rs.getString("name"); u.email = rs.getString("email"); u.phone = rs.getString("phone"); return u; }
    public boolean register(User u) throws SQLException {
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement("INSERT INTO users(name,email,phone,password) VALUES(?,?,?,?)")) {
            ps.setString(1, u.name); ps.setString(2, u.email); ps.setString(3, u.phone); ps.setString(4, hash(u.password)); return ps.executeUpdate() > 0;
        }
    }
    public User login(String email, String pass) throws SQLException {
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement("SELECT * FROM users WHERE email=? AND password=?")) {
            ps.setString(1, email); ps.setString(2, hash(pass)); try (ResultSet rs = ps.executeQuery()) { return rs.next() ? map(rs) : null; }
        }
    }
    public List<User> findAll() throws SQLException {
        List<User> l = new ArrayList<>();
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement("SELECT * FROM users"); ResultSet rs = ps.executeQuery()) { while (rs.next()) l.add(map(rs)); }
        return l;
    }
}
