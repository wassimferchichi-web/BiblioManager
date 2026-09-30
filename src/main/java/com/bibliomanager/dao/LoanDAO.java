package com.bibliomanager.dao;

import com.bibliomanager.models.Loan;
import com.bibliomanager.utils.DBConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class LoanDAO {

    private Connection connection;

    public LoanDAO() {
        this.connection = DBConnection.getConnection();
    }

    public List<Loan> getAllLoans() {
        List<Loan> loans = new ArrayList<>();
        String sql = "SELECT l.id, b.title, m.full_name, l.loan_date, l.due_date, " +
                "l.return_date, l.status, l.book_id, l.member_id " +
                "FROM loans l " +
                "JOIN books b ON l.book_id = b.id " +
                "JOIN members m ON l.member_id = m.id " +
                "ORDER BY l.loan_date DESC";
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                loans.add(new Loan(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("full_name"),
                        rs.getDate("loan_date").toLocalDate(),
                        rs.getDate("due_date").toLocalDate(),
                        rs.getDate("return_date") != null
                                ? rs.getDate("return_date").toLocalDate() : null,
                        rs.getString("status"),
                        rs.getInt("book_id"),
                        rs.getInt("member_id")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error getting loans: " + e.getMessage());
        }
        return loans;
    }

    public boolean issueLoan(int bookId, int memberId, LocalDate dueDate) {
        String sql = "INSERT INTO loans (book_id, member_id, loan_date, due_date, status) " +
                "VALUES (?, ?, ?, ?, 'ACTIVE')";
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setInt(1, bookId);
            stmt.setInt(2, memberId);
            stmt.setDate(3, Date.valueOf(LocalDate.now()));
            stmt.setDate(4, Date.valueOf(dueDate));
            stmt.executeUpdate();

            // Decrease available copies
            PreparedStatement update = connection.prepareStatement(
                    "UPDATE books SET available_copies = available_copies - 1 WHERE id = ?"
            );
            update.setInt(1, bookId);
            update.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error issuing loan: " + e.getMessage());
            return false;
        }
    }

    public boolean returnBook(int loanId, int bookId) {
        String sql = "UPDATE loans SET return_date=?, status='RETURNED' WHERE id=?";
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setDate(1, Date.valueOf(LocalDate.now()));
            stmt.setInt(2, loanId);
            stmt.executeUpdate();

            // Increase available copies
            PreparedStatement update = connection.prepareStatement(
                    "UPDATE books SET available_copies = available_copies + 1 WHERE id = ?"
            );
            update.setInt(1, bookId);
            update.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error returning book: " + e.getMessage());
            return false;
        }
    }

    public void updateOverdueLoans() {
        String sql = "UPDATE loans SET status='OVERDUE' " +
                "WHERE status='ACTIVE' AND due_date < CURDATE()";
        try {
            Statement stmt = connection.createStatement();
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            System.err.println("Error updating overdue: " + e.getMessage());
        }
    }

    // Get all books for combo box
    public java.util.Map<Integer, String> getAvailableBooks() {
        java.util.Map<Integer, String> books = new java.util.LinkedHashMap<>();
        String sql = "SELECT id, title FROM books WHERE available_copies > 0 ORDER BY title";
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                books.put(rs.getInt("id"), rs.getString("title"));
            }
        } catch (SQLException e) {
            System.err.println("Error getting available books: " + e.getMessage());
        }
        return books;
    }

    // Get all members for combo box
    public java.util.Map<Integer, String> getAllMembersMap() {
        java.util.Map<Integer, String> members = new java.util.LinkedHashMap<>();
        String sql = "SELECT id, full_name FROM members ORDER BY full_name";
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                members.put(rs.getInt("id"), rs.getString("full_name"));
            }
        } catch (SQLException e) {
            System.err.println("Error getting members: " + e.getMessage());
        }
        return members;
    }
}