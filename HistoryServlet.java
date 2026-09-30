package com.functionhall.controller;
import com.functionhall.dao.HistoryDAO; import com.functionhall.model.*;
import java.io.*; import java.util.*; import javax.servlet.*; import javax.servlet.annotation.WebServlet; import javax.servlet.http.*;
@WebServlet("/history")
public class HistoryServlet extends HttpServlet {
    private static String csv(String s) {
        if (s == null) s = "";
        if (s.matches("^[=+\\-@].*")) s = "'" + s;                 // block CSV formula injection
        return "\"" + s.replace("\"", "\"\"") + "\"";
    }
    private static String p(HttpServletRequest r, String n) { String v = r.getParameter(n); return v == null ? "" : v.trim(); }
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        User u = (User) req.getSession().getAttribute("user");
        if (u == null) { res.sendRedirect("login.jsp"); return; }   // history is private to the logged-in customer
        List<HistoryRecord> list;
        try { list = new HistoryDAO().search(u.phone, p(req, "q"), p(req, "status"), p(req, "method"), p(req, "food")); }
        catch (Exception e) { req.setAttribute("error", "Database error: " + e.getMessage()); list = new ArrayList<>(); }
        if ("csv".equals(req.getParameter("download"))) {
            res.setContentType("text/csv;charset=UTF-8"); res.setHeader("Content-Disposition", "attachment; filename=\"booking-history.csv\"");
            PrintWriter w = res.getWriter(); w.write('\uFEFF');
            w.println("Booking ID,Customer,Hall,Event Date,Food,Total Amount,Payment Method,Transaction ID,Payment Date,Payment Status,Booking Status,Refund Amount");
            for (HistoryRecord r : list) w.println(String.join(",", csv(r.bookingId), csv(r.customerName), csv(r.hallName), csv(r.eventDate), csv(r.foodType),
                String.valueOf(r.totalAmount), csv(r.paymentMethod), csv(r.transactionId), csv(r.paymentDate), csv(r.paymentStatus), csv(r.bookingStatus), String.valueOf(r.refundAmount)));
            return;
        }
        req.setAttribute("history", list); req.getRequestDispatcher("history.jsp").forward(req, res);
    }
}
