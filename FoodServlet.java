package com.functionhall.controller;
import com.functionhall.dao.*;
import java.io.IOException; import javax.servlet.*; import javax.servlet.annotation.WebServlet; import javax.servlet.http.*;
@WebServlet("/menu")
public class FoodServlet extends HttpServlet {
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        String type = req.getParameter("type"); if (!"Veg".equals(type) && !"NonVeg".equals(type)) type = null;
        try { req.setAttribute("foods", new FoodDAO().find(type)); req.setAttribute("halls", new HallDAO().findAll()); }
        catch (Exception e) { req.setAttribute("error", e.getMessage()); }
        req.setAttribute("type", type); req.getRequestDispatcher("menu.jsp").forward(req, res);
    }
}
