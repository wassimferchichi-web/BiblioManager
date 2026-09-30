package com.bibliomanager.controllers;

import com.bibliomanager.models.User;
import com.bibliomanager.utils.DBConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class DashboardController {

    @FXML private Label totalBooksLabel;
    @FXML private Label totalMembersLabel;
    @FXML private Label activeLoansLabel;
    @FXML private Label overdueLabel;
    @FXML private Label loggedUserLabel;
    @FXML private BarChart<String, Number> categoryChart;
    @FXML private PieChart statusChart;

    private User currentUser;

    public void setUser(User user) {
        this.currentUser = user;
        loggedUserLabel.setText("👤 " + user.getUsername() + " (" + user.getRole() + ")");
        loadStats();
        loadCategoryChart();
        loadStatusChart();
    }

    private void loadStats() {
        Connection conn = DBConnection.getConnection();
        try {
            Statement stmt = conn.createStatement();

            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM books");
            if (rs.next()) totalBooksLabel.setText(String.valueOf(rs.getInt(1)));

            rs = stmt.executeQuery("SELECT COUNT(*) FROM members");
            if (rs.next()) totalMembersLabel.setText(String.valueOf(rs.getInt(1)));

            rs = stmt.executeQuery("SELECT COUNT(*) FROM loans WHERE status = 'ACTIVE'");
            if (rs.next()) activeLoansLabel.setText(String.valueOf(rs.getInt(1)));

            rs = stmt.executeQuery("SELECT COUNT(*) FROM loans WHERE status = 'OVERDUE'");
            if (rs.next()) overdueLabel.setText(String.valueOf(rs.getInt(1)));

        } catch (Exception e) {
            System.err.println("Error loading stats: " + e.getMessage());
        }
    }

    private void loadCategoryChart() {
        Connection conn = DBConnection.getConnection();
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(
                    "SELECT COALESCE(c.name, 'Uncategorized') as category, COUNT(b.id) as total " +
                            "FROM books b LEFT JOIN categories c ON b.category_id = c.id " +
                            "GROUP BY c.name ORDER BY total DESC LIMIT 8"
            );

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Books");

            while (rs.next()) {
                series.getData().add(new XYChart.Data<>(
                        rs.getString("category"),
                        rs.getInt("total")
                ));
            }

            if (series.getData().isEmpty()) {
                series.getData().add(new XYChart.Data<>("No Data", 0));
            }

            categoryChart.getData().clear();
            categoryChart.getData().add(series);
            categoryChart.setLegendVisible(false);
            categoryChart.setAnimated(false);

            // Apply colors after data is loaded
            String[] colors = {
                    "#89b4fa", "#a6e3a1", "#f9e2af",
                    "#cba6f7", "#f38ba8", "#94e2d5",
                    "#fab387", "#eba0ac"
            };

            for (int i = 0; i < series.getData().size(); i++) {
                XYChart.Data<String, Number> data = series.getData().get(i);
                final String color = colors[i % colors.length];
                data.nodeProperty().addListener((obs, oldNode, newNode) -> {
                    if (newNode != null) {
                        newNode.setStyle("-fx-bar-fill: " + color + ";");
                    }
                });
                if (data.getNode() != null) {
                    data.getNode().setStyle("-fx-bar-fill: " + color + ";");
                }
            }

        } catch (Exception e) {
            System.err.println("Error loading category chart: " + e.getMessage());
        }
    }

    private void loadStatusChart() {
        Connection conn = DBConnection.getConnection();
        try {
            Statement stmt = conn.createStatement();

            int active = 0, returned = 0, overdue = 0;

            ResultSet rs = stmt.executeQuery(
                    "SELECT status, COUNT(*) as total FROM loans GROUP BY status"
            );
            while (rs.next()) {
                switch (rs.getString("status")) {
                    case "ACTIVE":   active   = rs.getInt("total"); break;
                    case "RETURNED": returned = rs.getInt("total"); break;
                    case "OVERDUE":  overdue  = rs.getInt("total"); break;
                }
            }

            ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();

            if (active > 0)   pieData.add(new PieChart.Data("Active ("   + active   + ")", active));
            if (returned > 0) pieData.add(new PieChart.Data("Returned (" + returned + ")", returned));
            if (overdue > 0)  pieData.add(new PieChart.Data("Overdue ("  + overdue  + ")", overdue));

            if (pieData.isEmpty()) {
                pieData.add(new PieChart.Data("No Loans Yet", 1));
            }

            statusChart.setData(pieData);
            statusChart.setAnimated(false);
            statusChart.setStyle("-fx-background-color: transparent;");

            // Apply colors to pie slices
            String[] pieColors = {"#89b4fa", "#a6e3a1", "#f38ba8"};
            for (int i = 0; i < statusChart.getData().size(); i++) {
                final String color = pieColors[i % pieColors.length];
                PieChart.Data slice = statusChart.getData().get(i);
                slice.nodeProperty().addListener((obs, oldNode, newNode) -> {
                    if (newNode != null) {
                        newNode.setStyle("-fx-pie-color: " + color + ";");
                    }
                });
                if (slice.getNode() != null) {
                    slice.getNode().setStyle("-fx-pie-color: " + color + ";");
                }
            }

        } catch (Exception e) {
            System.err.println("Error loading status chart: " + e.getMessage());
        }
    }

    @FXML
    private void goToBooks() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/books.fxml"));
            Scene scene = new Scene(loader.load(), 900, 600);
            BooksController controller = loader.getController();
            if (currentUser != null) controller.setUser(currentUser);
            Stage stage = (Stage) totalBooksLabel.getScene().getWindow();
            stage.setScene(scene);
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void goToMembers() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/members.fxml"));
            Scene scene = new Scene(loader.load(), 900, 600);
            MembersController controller = loader.getController();
            if (currentUser != null) controller.setUser(currentUser);
            Stage stage = (Stage) totalBooksLabel.getScene().getWindow();
            stage.setScene(scene);
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void goToLoans() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/loans.fxml"));
            Scene scene = new Scene(loader.load(), 900, 600);
            LoansController controller = loader.getController();
            if (currentUser != null) controller.setUser(currentUser);
            Stage stage = (Stage) totalBooksLabel.getScene().getWindow();
            stage.setScene(scene);
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void handleLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Scene scene = new Scene(loader.load(), 500, 600);
            Stage stage = (Stage) totalBooksLabel.getScene().getWindow();
            stage.setScene(scene);
            stage.setWidth(500);
            stage.setHeight(600);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}