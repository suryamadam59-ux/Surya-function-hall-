package com.functionhall.controller;
import com.functionhall.dao.*; import com.functionhall.model.*;
import java.io.IOException; import java.time.*;
import javax.servlet.*; import javax.servlet.annotation.WebServlet; import javax.servlet.http.*;
@WebServlet("/booking")
public class BookingServlet extends HttpServlet {
    static final double DECORATION = 15000, OTHER = 2000;
    static double foodRate(String t) { return t.equals("Veg") ? 350 : t.equals("NonVeg") ? 500 : 600; } // per guest (demo rates)

    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        try { req.setAttribute("halls", new HallDAO().findAll()); } catch (Exception e) { req.setAttribute("error", e.getMessage()); }
        req.getRequestDispatcher("booking.jsp").forward(req, res);
    }
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        String name = req.getParameter("name"), phone = req.getParameter("phone"), email = req.getParameter("email"),
            type = req.getParameter("eventType"), date = req.getParameter("eventDate"), start = req.getParameter("startTime"),
            end = req.getParameter("endTime"), food = req.getParameter("foodType");
        String err = null; int guests = 0, hallId = 0, days = 1;
        try { guests = Integer.parseInt(req.getParameter("guests")); hallId = Integer.parseInt(req.getParameter("hallId")); }
        catch (Exception e) { err = "Guests and hall are required."; }
        if (err == null) {
            if (name == null || name.trim().isEmpty() || type == null || type.isEmpty() || food == null) err = "Please fill all required fields.";
            else if (phone == null || !phone.matches("[6-9]\\d{9}")) err = "Invalid mobile number.";
            else if (email == null || !email.matches("^[\\w.+-]+@[\\w-]+\\.[\\w.-]+$")) err = "Invalid email.";
            else if (guests < 1) err = "Guest count must be at least 1.";
            else try {
                if (LocalDate.parse(date).isBefore(LocalDate.now())) err = "Event date cannot be in the past.";
                else if (!LocalTime.parse(end).isAfter(LocalTime.parse(start))) err = "End time must be after start time.";
            } catch (Exception e) { err = "Invalid date or time."; }
        }
        try {
            HallDAO hd = new HallDAO(); BookingDAO bd = new BookingDAO(); Hall hall = null;
            if (err == null) { hall = hd.findById(hallId); if (hall == null) err = "Hall not found."; else if (guests > hall.capacity) err = "Guests exceed hall capacity (" + hall.capacity + ")."; }
            if (err == null && bd.isHallBooked(hallId, date, start + ":00", end + ":00")) err = "This hall is already booked for that time.";
            if (err == null) {
                Booking b = new Booking(); b.bookingId = "SFH" + System.currentTimeMillis() % 100000000L;
                b.customerName = name.trim(); b.phone = phone; b.email = email; b.hallId = hallId; b.hallName = hall.hallName;
                b.eventType = type; b.eventDate = date; b.startTime = start + ":00"; b.endTime = end + ":00"; b.guests = guests; b.foodType = food;
                b.decoration = "on".equals(req.getParameter("decoration"));
                double hallCost = hall.price * days, foodCost = foodRate(food) * guests, deco = b.decoration ? DECORATION : 0;
                b.total = hallCost + foodCost + deco + OTHER; b.status = "Pending";
                bd.create(b);
                req.setAttribute("booking", b);
                req.setAttribute("bill", new double[]{hallCost, foodCost, deco, OTHER, b.total});
            }
            req.setAttribute("halls", hd.findAll());
        } catch (Exception e) { err = "Database error: " + e.getMessage(); }
        req.setAttribute("error", err);
        req.getRequestDispatcher("booking.jsp").forward(req, res);
    }
}
