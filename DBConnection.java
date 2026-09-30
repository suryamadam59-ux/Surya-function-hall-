package com.functionhall.util;
import java.sql.*;
public class DBConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/function_hall?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root", PASS = "root"; // change to your MySQL login
    public static Connection getConnection() throws SQLException {
        try { Class.forName("com.mysql.cj.jdbc.Driver"); }
        catch (ClassNotFoundException e) { throw new SQLException("MySQL driver missing", e); }
        return DriverManager.getConnection(URL, USER, PASS);
    }
}
