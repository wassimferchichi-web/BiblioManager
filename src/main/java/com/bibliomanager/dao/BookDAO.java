package com.bibliomanager.dao;

import com.bibliomanager.models.Book;
import com.bibliomanager.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class BookDAO {

    private Connection connection;

    public BookDAO() {
        this.connection = DBConnection.getConnection();
    }

    public List<Book> getAllBooks() {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT b.id, b.title, b.author, b.isbn, " +
                "COALESCE(c.name, 'Uncategorized') as category, " +
                "b.total_copies, b.available_copies " +
                "FROM books b LEFT JOIN categories c ON b.category_id = c.id " +
                "ORDER BY b.title";
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                books.add(new Book(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("author"),
                        rs.getString("isbn"),
                        rs.getString("category"),
                        rs.getInt("total_copies"),
                        rs.getInt("available_copies")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error getting books: " + e.getMessage());
        }
        return books;
    }

    public List<Book> searchBooks(String keyword) {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT b.id, b.title, b.author, b.isbn, " +
                "COALESCE(c.name, 'Uncategorized') as category, " +
                "b.total_copies, b.available_copies " +
                "FROM books b LEFT JOIN categories c ON b.category_id = c.id " +
                "WHERE b.title LIKE ? OR b.author LIKE ? " +
                "ORDER BY b.title";
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1, "%" + keyword + "%");
            stmt.setString(2, "%" + keyword + "%");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                books.add(new Book(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("author"),
                        rs.getString("isbn"),
                        rs.getString("category"),
                        rs.getInt("total_copies"),
                        rs.getInt("available_copies")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error searching books: " + e.getMessage());
        }
        return books;
    }

    public Map<Integer, String> getCategoriesMap() {
        Map<Integer, String> map = new LinkedHashMap<>();
        String sql = "SELECT id, name FROM categories ORDER BY name";
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                map.put(rs.getInt("id"), rs.getString("name"));
            }
        } catch (SQLException e) {
            System.err.println("Error getting categories: " + e.getMessage());
        }
        return map;
    }

    public boolean addBook(String title, String author, String isbn,
                           int totalCopies, int categoryId) {
        String sql = "INSERT INTO books (title, author, isbn, total_copies, " +
                "available_copies, category_id) VALUES (?, ?, ?, ?, ?, ?)";
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1, title);
            stmt.setString(2, author);
            stmt.setString(3, isbn);
            stmt.setInt(4, totalCopies);
            stmt.setInt(5, totalCopies);
            if (categoryId > 0) {
                stmt.setInt(6, categoryId);
            } else {
                stmt.setNull(6, Types.INTEGER);
            }
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error adding book: " + e.getMessage());
            return false;
        }
    }

    public boolean updateBook(int id, String title, String author,
                              String isbn, int totalCopies, int categoryId) {
        String sql = "UPDATE books SET title=?, author=?, isbn=?, " +
                "total_copies=?, category_id=? WHERE id=?";
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1, title);
            stmt.setString(2, author);
            stmt.setString(3, isbn);
            stmt.setInt(4, totalCopies);
            if (categoryId > 0) {
                stmt.setInt(5, categoryId);
            } else {
                stmt.setNull(5, Types.INTEGER);
            }
            stmt.setInt(6, id);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error updating book: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteBook(int id) {
        String sql = "DELETE FROM books WHERE id=?";
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setInt(1, id);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error deleting book: " + e.getMessage());
            return false;
        }
    }
}