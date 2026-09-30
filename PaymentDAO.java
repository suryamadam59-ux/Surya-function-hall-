package com.functionhall.dao;
import com.functionhall.model.Payment; import com.functionhall.util.DBConnection;
import java.sql.*; import java.util.*;
public class PaymentDAO {
    public boolean add(Payment p) throws SQLException {          // INSERT payment with transaction ID
        String sql = "INSERT INTO payments(booking_id,amount,payment_method,payment_status,transaction_id) VALUES(?,?,?,?,?)";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, p.bookingPk); ps.setDouble(2, p.amount); ps.setString(3, p.method); ps.setString(4, p.status); ps.setString(5, p.transactionId);
            return ps.executeUpdate() > 0;
        }
    }
    public boolean isPaid(int bookingPk) throws SQLException {
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM payments WHERE booking_id=? AND payment_status='Paid'")) {
            ps.setInt(1, bookingPk); try (ResultSet rs = ps.executeQuery()) { rs.next(); return rs.getInt(1) > 0; }
        }
    }
    public List<Payment> findAll() throws SQLException {          // JOIN payments + bookings (admin)
        List<Payment> l = new ArrayList<>();
        String sql = "SELECT p.*, b.booking_id AS bid FROM payments p JOIN bookings b ON p.booking_id=b.id ORDER BY p.payment_date DESC";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) { Payment p = new Payment(); p.paymentId = rs.getInt("payment_id"); p.bookingId = rs.getString("bid"); p.amount = rs.getDouble("amount");
                p.method = rs.getString("payment_method"); p.status = rs.getString("payment_status"); p.date = rs.getString("payment_date"); p.transactionId = rs.getString("transaction_id"); l.add(p); }
        }
        return l;
    }
}
