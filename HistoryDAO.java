package com.functionhall.dao;
import com.functionhall.model.HistoryRecord; import com.functionhall.util.DBConnection;
import java.sql.*; import java.util.*;
public class HistoryDAO {
    /** Customer's history (matched by phone). Empty/null filters are ignored. status: Paid|Pending|Unpaid. */
    public List<HistoryRecord> search(String phone, String q, String status, String method, String food) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT b.booking_id,b.customer_name,h.hall_name,b.event_date,b.food_type,b.total_amount,b.status AS bstatus,"
            + "p.payment_method,p.transaction_id,p.payment_date,p.payment_status,p.refund_amount FROM bookings b JOIN halls h ON b.hall_id=h.id "
            + "LEFT JOIN payments p ON p.booking_id=b.id WHERE b.phone=?");
        List<String> args = new ArrayList<>(); args.add(phone);
        if (q != null && !q.isEmpty()) { sql.append(" AND (b.booking_id LIKE ? OR h.hall_name LIKE ? OR p.transaction_id LIKE ? OR b.customer_name LIKE ?)"); for (int i = 0; i < 4; i++) args.add("%" + q + "%"); }
        if ("Unpaid".equals(status)) sql.append(" AND p.payment_id IS NULL");
        else if (status != null && !status.isEmpty()) { sql.append(" AND p.payment_status=?"); args.add(status); }
        if (method != null && !method.isEmpty()) { sql.append(" AND p.payment_method=?"); args.add(method); }
        if (food != null && !food.isEmpty()) { sql.append(" AND b.food_type=?"); args.add(food); }
        sql.append(" ORDER BY COALESCE(p.payment_date,b.created_at) DESC");
        List<HistoryRecord> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql.toString())) {
            for (int i = 0; i < args.size(); i++) ps.setString(i + 1, args.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) { HistoryRecord r = new HistoryRecord(); r.bookingId = rs.getString("booking_id"); r.customerName = rs.getString("customer_name");
                    r.hallName = rs.getString("hall_name"); r.eventDate = rs.getString("event_date"); r.foodType = rs.getString("food_type"); r.totalAmount = rs.getDouble("total_amount");
                    r.bookingStatus = rs.getString("bstatus"); r.paymentMethod = rs.getString("payment_method"); r.transactionId = rs.getString("transaction_id");
                    r.paymentDate = rs.getString("payment_date"); r.refundAmount = rs.getDouble("refund_amount"); r.paymentStatus = rs.getString("payment_status") == null ? "Unpaid" : rs.getString("payment_status"); list.add(r); }
            }
        }
        return list;
    }
}
