package com.functionhall.controller;
import com.functionhall.dao.BookingDAO; import com.functionhall.model.User;
import java.io.IOException; import javax.servlet.*; import javax.servlet.annotation.WebServlet; import javax.servlet.http.*;
@WebServlet("/cancel")
public class CancelBookingServlet extends HttpServlet {
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        User u = (User) req.getSession().getAttribute("user");
        if (u == null) { res.sendRedirect("login.jsp"); return; }
        String id = req.getParameter("bookingId"), msg;
        try {
            double refund = new BookingDAO().cancelWithRefund(id, u.phone);   // phone check: customers cancel only their own bookings
            msg = refund < 0 ? "This booking cannot be cancelled." : refund > 0 ? "Booking " + id + " cancelled. Refund of Rs. " + refund + " (50%) initiated."
                : "Booking " + id + " cancelled. No payment was made, so no refund.";
        } catch (Exception e) { msg = "Cancel failed: " + e.getMessage(); }
        req.getSession().setAttribute("flash", msg); res.sendRedirect("history");
    }
}
