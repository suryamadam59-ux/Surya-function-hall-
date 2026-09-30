package com.functionhall.dao;
import com.functionhall.model.Booking;
import com.functionhall.util.DBConnection;
import java.sql.*;
public class BookingDAO {
    /** Double-booking check: overlapping time on same hall/date, ignoring cancelled bookings. */
    public boolean isHallBooked(int hallId, String date, String start, String end) throws SQLException {
        String sql = "SELECT COUNT(*) FROM bookings WHERE hall_id=? AND event_date=? AND status<>'Cancelled' AND start_time<? AND end_time>?";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, hallId); ps.setDate(2, Date.valueOf(date)); ps.setTime(3, Time.valueOf(end)); ps.setTime(4, Time.valueOf(start));
            try (ResultSet rs = ps.executeQuery()) { rs.next(); return rs.getInt(1) > 0; }
        }
    }
    public boolean create(Booking b) throws SQLException {                  // CREATE
        String sql = "INSERT INTO bookings(booking_id,customer_name,phone,email,hall_id,event_type,event_date,start_time,end_time,guests,food_type,decoration,total_amount,status) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,'Pending')";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, b.bookingId); ps.setString(2, b.customerName); ps.setString(3, b.phone); ps.setString(4, b.email);
            ps.setInt(5, b.hallId); ps.setString(6, b.eventType); ps.setDate(7, Date.valueOf(b.eventDate));
            ps.setTime(8, Time.valueOf(b.startTime)); ps.setTime(9, Time.valueOf(b.endTime)); ps.setInt(10, b.guests);
            ps.setString(11, b.foodType); ps.setBoolean(12, b.decoration); ps.setDouble(13, b.total);
            return ps.executeUpdate() > 0;
        }
    }
    /** SEARCH/TRACK: JOIN bookings + halls; requires booking ID AND phone to match. */
    public Booking findByIdAndPhone(String bookingId, String phone) throws SQLException {
        String sql = "SELECT b.*, h.hall_name FROM bookings b JOIN halls h ON b.hall_id=h.id WHERE b.booking_id=? AND b.phone=?";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, bookingId); ps.setString(2, phone);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                Booking b = new Booking(); b.id = rs.getInt("id"); b.phone = phone; b.bookingId = rs.getString("booking_id"); b.customerName = rs.getString("customer_name");
                b.hallName = rs.getString("hall_name"); b.eventType = rs.getString("event_type"); b.eventDate = rs.getString("event_date");
                b.guests = rs.getInt("guests"); b.foodType = rs.getString("food_type"); b.total = rs.getDouble("total_amount");
                b.status = rs.getString("status"); return b;
            }
        }
    }
    public boolean updateStatus(String bookingId, String status) throws SQLException { // UPDATE / CANCEL (admin)
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement("UPDATE bookings SET status=? WHERE booking_id=?")) {
            ps.setString(1, status); ps.setString(2, bookingId); return ps.executeUpdate() > 0;
        }
    }
    public java.util.List<Booking> findAll() throws SQLException {          // admin: JOIN bookings + halls
        java.util.List<Booking> l = new java.util.ArrayList<>();
        String sql = "SELECT b.*, h.hall_name FROM bookings b JOIN halls h ON b.hall_id=h.id ORDER BY b.created_at DESC";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) { Booking b = new Booking(); b.bookingId = rs.getString("booking_id"); b.customerName = rs.getString("customer_name"); b.phone = rs.getString("phone");
                b.hallName = rs.getString("hall_name"); b.eventDate = rs.getString("event_date"); b.guests = rs.getInt("guests"); b.total = rs.getDouble("total_amount"); b.status = rs.getString("status"); l.add(b); }
        }
        return l;
    }
    /** {halls, foods, bookings, pending, confirmed, revenue} */
    public double[] stats() throws SQLException {
        String sql = "SELECT (SELECT COUNT(*) FROM halls),(SELECT COUNT(*) FROM food_items),(SELECT COUNT(*) FROM bookings),"
            + "(SELECT COUNT(*) FROM bookings WHERE status='Pending'),(SELECT COUNT(*) FROM bookings WHERE status='Confirmed'),"
            + "(SELECT COALESCE(SUM(amount),0) FROM payments WHERE payment_status='Paid')";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            rs.next(); double[] s = new double[6]; for (int i = 0; i < 6; i++) s[i] = rs.getDouble(i + 1); return s;
        }
    }
    public static final double REFUND_PERCENT = 0.5;   // 50% refund on cancellation of a paid booking
    /** Cancels a Pending/Confirmed booking in ONE transaction; if a Paid payment exists, refunds 50%. phone=null for admin.
     *  Returns refund amount (0 if nothing was paid) or -1 if the booking cannot be cancelled. */
    public double cancelWithRefund(String bookingId, String phone) throws SQLException {
        try (Connection c = DBConnection.getConnection()) {
            c.setAutoCommit(false);
            try {
                int pk;
                String sel = "SELECT id,status FROM bookings WHERE booking_id=?" + (phone != null ? " AND phone=?" : "") + " FOR UPDATE";
                try (PreparedStatement ps = c.prepareStatement(sel)) {
                    ps.setString(1, bookingId); if (phone != null) ps.setString(2, phone);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next() || !(rs.getString("status").equals("Pending") || rs.getString("status").equals("Confirmed"))) { c.rollback(); return -1; }
                        pk = rs.getInt("id");
                    }
                }
                try (PreparedStatement ps = c.prepareStatement("UPDATE bookings SET status='Cancelled' WHERE id=?")) { ps.setInt(1, pk); ps.executeUpdate(); }
                double refund = 0;
                try (PreparedStatement ps = c.prepareStatement("SELECT payment_id,amount FROM payments WHERE booking_id=? AND payment_status='Paid' AND refund_status='None'")) {
                    ps.setInt(1, pk);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            refund = Math.round(rs.getDouble("amount") * REFUND_PERCENT * 100) / 100.0;
                            try (PreparedStatement up = c.prepareStatement("UPDATE payments SET refund_amount=?,refund_status='Refunded',refund_date=NOW(),payment_status='Refunded' WHERE payment_id=?")) {
                                up.setDouble(1, refund); up.setInt(2, rs.getInt("payment_id")); up.executeUpdate();
                            }
                        }
                    }
                }
                c.commit(); return refund;
            } catch (SQLException e) { c.rollback(); throw e; }
        }
    }
}
