package com.functionhall.controller;
import com.functionhall.dao.BookingDAO;
import java.io.IOException;
import javax.servlet.*; import javax.servlet.annotation.WebServlet; import javax.servlet.http.*;
@WebServlet("/track")
public class TrackBookingServlet extends HttpServlet {
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        req.getRequestDispatcher("track-booking.jsp").forward(req, res);
    }
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        try {
            var b = new BookingDAO().findByIdAndPhone(req.getParameter("bookingId").trim(), req.getParameter("phone").trim());
            if (b == null) req.setAttribute("error", "No booking found for that ID and mobile number."); else req.setAttribute("booking", b);
        } catch (Exception e) { req.setAttribute("error", "Database error: " + e.getMessage()); }
        req.getRequestDispatcher("track-booking.jsp").forward(req, res);
    }
}
