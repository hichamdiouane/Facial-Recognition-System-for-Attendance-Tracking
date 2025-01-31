package ma.enset.presentation.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    // Handle login action
    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Error", "Username and Password cannot be empty.");
        } else if (validateCredentials(username, password)) {
            // Proceed with login (e.g., open main application window)
            System.out.println("Login successful");
        } else {
            showAlert("Error", "Invalid credentials. Please try again.");
        }
    }

    // Handle register action (for now, just shows a message)
    @FXML
    private void handleRegister() {
        // You can implement registration functionality here
        System.out.println("Redirecting to registration page...");
    }

    // Method to validate login credentials (for demonstration purposes, using hardcoded credentials)
    private boolean validateCredentials(String username, String password) {
        // For now, hardcoded validation (you can integrate with a database or authentication service)
        return "admin".equals(username) && "password123".equals(password);
    }

    // Utility method to show alert messages
    private void showAlert(String title, String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
