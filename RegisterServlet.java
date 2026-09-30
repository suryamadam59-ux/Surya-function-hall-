package com.functionhall.controller;
import com.functionhall.dao.UserDAO; import com.functionhall.model.User;
import java.io.IOException; import javax.servlet.*; import javax.servlet.annotation.WebServlet; import javax.servlet.http.*;
@WebServlet("/register")
public class RegisterServlet extends HttpServlet {
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        User u = new User(); u.name = req.getParameter("name"); u.email = req.getParameter("email"); u.phone = req.getParameter("phone"); u.password = req.getParameter("password");
        String err = null;
        if (u.name == null || u.name.trim().isEmpty()) err = "Name required.";
        else if (u.email == null || !u.email.matches("^[\\w.+-]+@[\\w-]+\\.[\\w.-]+$")) err = "Invalid email.";
        else if (u.phone == null || !u.phone.matches("[6-9]\\d{9}")) err = "Invalid mobile number.";
        else if (u.password == null || u.password.length() < 6) err = "Password must be at least 6 characters.";
        if (err == null) try { new UserDAO().register(u); res.sendRedirect("login.jsp?registered=1"); return; }
            catch (java.sql.SQLException e) { err = "Registration failed (email may already exist)."; }
        req.setAttribute("error", err); req.getRequestDispatcher("register.jsp").forward(req, res);
    }
}
