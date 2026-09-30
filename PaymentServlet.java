package com.functionhall.controller;
import com.functionhall.dao.*; import com.functionhall.model.*;
import java.io.IOException; import javax.servlet.*; import javax.servlet.annotation.WebServlet; import javax.servlet.http.*;
@WebServlet("/payment")
public class PaymentServlet extends HttpServlet {
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException { req.getRequestDispatcher("payment.jsp").forward(req, res); }
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        String method = req.getParameter("method");
        try {
            Booking b = new BookingDAO().findByIdAndPhone(req.getParameter("bookingId").trim(), req.getParameter("phone").trim());
            PaymentDAO dao = new PaymentDAO();
            if (b == null || method == null || !method.matches("Cash|UPI|Card|Bank Transfer")) req.setAttribute("error", "Booking not found or invalid method.");
            else if ("Cancelled".equals(b.status)) req.setAttribute("error", "Cancelled bookings cannot be paid.");
            else if (dao.isPaid(b.id)) req.setAttribute("error", "This booking is already paid.");
            else {                                    // MOCK gateway: Cash stays Pending, others succeed instantly
                Payment p = new Payment(); p.bookingPk = b.id; p.amount = b.total; p.method = method; p.status = method.equals("Cash") ? "Pending" : "Paid";
                p.transactionId = "TXN" + System.currentTimeMillis() + (int) (Math.random() * 90 + 10);
                dao.add(p);                           // saved automatically -> appears in History
                req.setAttribute("msg", "Payment " + p.status + ". Transaction ID: " + p.transactionId + " (Rs. " + b.total + ")");
            }
        } catch (Exception e) { req.setAttribute("error", "Error: " + e.getMessage()); }
        req.getRequestDispatcher("payment.jsp").forward(req, res);
    }
}
