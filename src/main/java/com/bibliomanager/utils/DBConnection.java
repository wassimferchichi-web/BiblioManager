package com.bibliomanager.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private static final String URL      = "jdbc:mysql://localhost:3306/bibliomanager";
    private static final String USER     = "root";
    private static final String PASSWORD = "";

    private static Connection connection = null;

    private DBConnection() {}

    public static Connection getConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("✅ Database connected!");
            }
        } catch (ClassNotFoundException e) {
            System.err.println("❌ Driver not found: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("❌ Connection failed: " + e.getMessage());
        }
        return connection;
    }
}