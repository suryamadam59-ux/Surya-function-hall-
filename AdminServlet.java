package com.functionhall.controller;
import com.functionhall.dao.*; import com.functionhall.model.*;
import java.io.IOException; import javax.servlet.*; import javax.servlet.annotation.WebServlet; import javax.servlet.http.*;
@WebServlet("/admin")
public class AdminServlet extends HttpServlet {
    private boolean isAdmin(HttpServletRequest r) { return r.getSession().getAttribute("admin") != null; }
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        if (!isAdmin(req)) { res.sendRedirect("login.jsp"); return; }
        try {
            req.setAttribute("stats", new BookingDAO().stats()); req.setAttribute("bookings", new BookingDAO().findAll());
            req.setAttribute("halls", new HallDAO().findAll()); req.setAttribute("foods", new FoodDAO().find(null));
            req.setAttribute("customers", new UserDAO().findAll()); req.setAttribute("payments", new PaymentDAO().findAll());
        } catch (Exception e) { req.setAttribute("error", e.getMessage()); }
        req.getRequestDispatcher("admin/dashboard.jsp").forward(req, res);
    }
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        if (!isAdmin(req)) { res.sendRedirect("login.jsp"); return; }
        String a = req.getParameter("action");
        try {
            if ("status".equals(a) && req.getParameter("status").matches("Pending|Confirmed|Cancelled|Completed"))
                { if ("Cancelled".equals(req.getParameter("status"))) new BookingDAO().cancelWithRefund(req.getParameter("bookingId"), null);   // admin cancel also refunds 50%
                else new BookingDAO().updateStatus(req.getParameter("bookingId"), req.getParameter("status")); }
            else if ("addHall".equals(a)) { Hall h = new Hall(); h.hallName = req.getParameter("name"); h.capacity = Integer.parseInt(req.getParameter("capacity"));
                h.price = Double.parseDouble(req.getParameter("price")); h.ac = req.getParameter("ac") != null; h.parking = true; h.stage = req.getParameter("stage") != null; h.status = "Available"; new HallDAO().add(h); }
            else if ("deleteHall".equals(a)) new HallDAO().delete(Integer.parseInt(req.getParameter("id")));
            else if ("addFood".equals(a)) { FoodItem f = new FoodItem(); f.itemName = req.getParameter("name"); f.category = req.getParameter("category");
                f.foodType = "NonVeg".equals(req.getParameter("type")) ? "NonVeg" : "Veg"; f.price = Double.parseDouble(req.getParameter("price")); new FoodDAO().add(f); }
            else if ("deleteFood".equals(a)) new FoodDAO().delete(Integer.parseInt(req.getParameter("id")));
        } catch (Exception e) { req.getSession().setAttribute("flash", "Action failed: " + e.getMessage()); }
        res.sendRedirect("admin");
    }
}
