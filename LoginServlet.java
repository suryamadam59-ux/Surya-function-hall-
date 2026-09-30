package com.functionhall.controller;
import com.functionhall.dao.UserDAO; import com.functionhall.model.User;
import java.io.IOException; import javax.servlet.*; import javax.servlet.annotation.WebServlet; import javax.servlet.http.*;
@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    static final String ADMIN_EMAIL = "admin@surya.com", ADMIN_PASS = "admin123";   // demo admin
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        req.getSession().invalidate(); res.sendRedirect("index.jsp");                // logout
    }
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        String email = req.getParameter("email"), pass = req.getParameter("password");
        if (email == null || pass == null) { res.sendRedirect("login.jsp"); return; }
        if (ADMIN_EMAIL.equals(email) && ADMIN_PASS.equals(pass)) { req.getSession().setAttribute("admin", true); res.sendRedirect("admin"); return; }
        try {
            User u = new UserDAO().login(email, pass);
            if (u != null) { req.getSession().setAttribute("user", u); res.sendRedirect("index.jsp"); return; }
            req.setAttribute("error", "Invalid email or password.");
        } catch (Exception e) { req.setAttribute("error", "Database error: " + e.getMessage()); }
        req.getRequestDispatcher("login.jsp").forward(req, res);
    }
}
