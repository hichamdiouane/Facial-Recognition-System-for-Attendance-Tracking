package ma.enset.app;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.sql.*;
import java.util.function.Consumer;

public class RegisterInterface {
    private VBox content;
    private TextField usernameField;
    private PasswordField passwordField;
    private PasswordField confirmPasswordField;

    public RegisterInterface(Consumer<Stage> onLoginRequest) {
        content = new VBox(15);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(20));
        content.getStyleClass().add("register-content");

        Label titleLabel = new Label("Prof Registration");
        titleLabel.getStyleClass().add("title-label");

        usernameField = new TextField();
        usernameField.setPromptText("Username");
        usernameField.getStyleClass().add("input-field");

        passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.getStyleClass().add("input-field");

        confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Confirm Password");
        confirmPasswordField.getStyleClass().add("input-field");

        Button registerButton = new Button("Register");
        registerButton.getStyleClass().add("action-button");
        registerButton.setOnAction(e -> {
            if (validateInputs() && register()) {
                showAlert("Registration Successful", "You can now login with your credentials.", Alert.AlertType.INFORMATION);
                onLoginRequest.accept((Stage) content.getScene().getWindow());
            }
        });

        Hyperlink loginLink = new Hyperlink("Already have an account? Login here");
        loginLink.setOnAction(e -> onLoginRequest.accept((Stage) content.getScene().getWindow()));

        content.getChildren().addAll(titleLabel, usernameField,
                passwordField, confirmPasswordField, registerButton, loginLink);
    }

    private boolean validateInputs() {
        // Validate username
        if (usernameField.getText().trim().isEmpty()) {
            showAlert("Validation Error", "Username cannot be empty", Alert.AlertType.ERROR);
            return false;
        }

        // Validate password
        if (passwordField.getText().length() < 6) {
            showAlert("Validation Error", "Password must be at least 6 characters long", Alert.AlertType.ERROR);
            return false;
        }

        // Validate password confirmation
        if (!passwordField.getText().equals(confirmPasswordField.getText())) {
            showAlert("Validation Error", "Passwords do not match", Alert.AlertType.ERROR);
            return false;
        }

        return true;
    }

    private boolean register() {
        try {
            Connection connection = SingletonConnexionDB.getConnection();

            // Insert new user
            String insertQuery = """
                INSERT INTO professeurs (username, password)
                VALUES (?, ?)
            """;

            try (PreparedStatement pstmt = connection.prepareStatement(insertQuery)) {
                pstmt.setString(1, usernameField.getText().trim());
                pstmt.setString(2, passwordField.getText());

                pstmt.executeUpdate();
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Failed to register professeur: " + e.getMessage(), Alert.AlertType.ERROR);
            return false;
        }
    }

    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public VBox getContent() {
        return content;
    }
}