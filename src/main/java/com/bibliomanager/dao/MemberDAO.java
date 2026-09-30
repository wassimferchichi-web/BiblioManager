package com.bibliomanager.dao;

import com.bibliomanager.models.Member;
import com.bibliomanager.utils.DBConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class MemberDAO {

    private Connection connection;

    public MemberDAO() {
        this.connection = DBConnection.getConnection();
    }

    public List<Member> getAllMembers() {
        List<Member> members = new ArrayList<>();
        String sql = "SELECT * FROM members ORDER BY full_name";
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                members.add(new Member(
                        rs.getInt("id"),
                        rs.getString("full_name"),
                        rs.getString("email"),
                        rs.getString("phone"),
                        rs.getDate("member_since") != null
                                ? rs.getDate("member_since").toLocalDate()
                                : LocalDate.now()
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error getting members: " + e.getMessage());
        }
        return members;
    }

    public List<Member> searchMembers(String keyword) {
        List<Member> members = new ArrayList<>();
        String sql = "SELECT * FROM members WHERE full_name LIKE ? OR email LIKE ? ORDER BY full_name";
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1, "%" + keyword + "%");
            stmt.setString(2, "%" + keyword + "%");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                members.add(new Member(
                        rs.getInt("id"),
                        rs.getString("full_name"),
                        rs.getString("email"),
                        rs.getString("phone"),
                        rs.getDate("member_since") != null
                                ? rs.getDate("member_since").toLocalDate()
                                : LocalDate.now()
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error searching members: " + e.getMessage());
        }
        return members;
    }

    public boolean addMember(String fullName, String email, String phone) {
        String sql = "INSERT INTO members (full_name, email, phone, member_since) VALUES (?, ?, ?, ?)";
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1, fullName);
            stmt.setString(2, email);
            stmt.setString(3, phone);
            stmt.setDate(4, Date.valueOf(LocalDate.now()));
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error adding member: " + e.getMessage());
            return false;
        }
    }

    public boolean updateMember(int id, String fullName, String email, String phone) {
        String sql = "UPDATE members SET full_name=?, email=?, phone=? WHERE id=?";
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1, fullName);
            stmt.setString(2, email);
            stmt.setString(3, phone);
            stmt.setInt(4, id);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error updating member: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteMember(int id) {
        String sql = "DELETE FROM members WHERE id=?";
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setInt(1, id);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error deleting member: " + e.getMessage());
            return false;
        }
    }
}