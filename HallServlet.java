package com.functionhall.controller;
import com.functionhall.dao.HallDAO;
import java.io.IOException; import javax.servlet.*; import javax.servlet.annotation.WebServlet; import javax.servlet.http.*;
@WebServlet("/halls")
public class HallServlet extends HttpServlet {
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        try { req.setAttribute("halls", new HallDAO().findAll()); } catch (Exception e) { req.setAttribute("error", e.getMessage()); }
        req.getRequestDispatcher("halls.jsp").forward(req, res);
    }
}
