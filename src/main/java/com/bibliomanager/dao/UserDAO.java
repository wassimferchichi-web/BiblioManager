package com.bibliomanager.dao;

import com.bibliomanager.models.User;
import com.bibliomanager.utils.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO {

    private Connection connection;

    public UserDAO() {
        this.connection = DBConnection.getConnection();
    }

    public User findByUsername(String username) {
        String sql = "SELECT u.id, u.username, u.password, u.email, r.name as role " +
                "FROM users u " +
                "JOIN roles r ON u.role_id = r.id " +
                "WHERE u.username = ?";
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("email"),
                        rs.getString("role")
                );
            }
        } catch (SQLException e) {
            System.err.println("❌ Error finding user: " + e.getMessage());
        }
        return null;
    }
}