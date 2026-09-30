package com.bibliomanager.controllers;

import com.bibliomanager.dao.UserDAO;
import com.bibliomanager.models.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    private UserDAO userDAO = new UserDAO();

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Please fill in all fields.");
            return;
        }

        User user = userDAO.findByUsername(username);

        if (user == null) {
            errorLabel.setText("❌ User not found.");
            return;
        }

        if (BCrypt.checkpw(password, user.getPassword())) {
            try {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/fxml/dashboard.fxml")
                );
                Scene scene = new Scene(loader.load(), 900, 600);
                DashboardController controller = loader.getController();
                controller.setUser(user);
                Stage stage = (Stage) usernameField.getScene().getWindow();
                stage.setScene(scene);
                stage.setWidth(900);
                stage.setHeight(600);
            } catch (Exception e) {
                e.printStackTrace();
                errorLabel.setText("Error loading dashboard.");
            }
        } else {
            errorLabel.setText("❌ Wrong password.");
        }
    }
}