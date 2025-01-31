package ma.enset.app;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.sql.*;
import java.util.function.Consumer;

public class LoginInterface {
    private VBox content;
    private TextField usernameField;
    private PasswordField passwordField;

    public LoginInterface(Consumer<Stage> onLoginSuccess, Consumer<Stage> onRegisterRequest) {
        content = new VBox(15);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(20));
        content.getStyleClass().add("login-content");

        Label titleLabel = new Label("Prof Login");
        titleLabel.getStyleClass().add("title-label");

        usernameField = new TextField();
        usernameField.setPromptText("Username");
        usernameField.getStyleClass().add("input-field");

        passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.getStyleClass().add("input-field");

        Button loginButton = new Button("Login");
        loginButton.getStyleClass().add("action-button");
        loginButton.setOnAction(e -> {
            String username = usernameField.getText();
            String password = passwordField.getText();
            if (isValidLogin(username, password)) {
                onLoginSuccess.accept((Stage) content.getScene().getWindow());
            } else {
                showAlert("Login Failed", "Invalid username or password.", Alert.AlertType.ERROR);
            }
        });

        Hyperlink registerLink = new Hyperlink("Don't have an account? Register here");
        registerLink.setOnAction(e -> onRegisterRequest.accept((Stage) content.getScene().getWindow()));

        content.getChildren().addAll(titleLabel, usernameField, passwordField, loginButton, registerLink);
    }

    public VBox getContent() {
        return content;
    }

    private boolean isValidLogin(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            showAlert("Validation Error", "Username cannot be empty", Alert.AlertType.ERROR);
            return false;
        }
        if (password == null || password.trim().isEmpty()) {
            showAlert("Validation Error", "Password cannot be empty", Alert.AlertType.ERROR);
            return false;
        }

        try {
            Connection connection = SingletonConnexionDB.getConnection();
            if (connection == null || connection.isClosed()) {
                showAlert("Database Error", "Failed to connect to the database.", Alert.AlertType.ERROR);
                return false;
            }
            String query = "SELECT * FROM professeurs WHERE username = ? AND password = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(query)) {
                pstmt.setString(1, username.trim());
                pstmt.setString(2, password);

                ResultSet rs = pstmt.executeQuery();
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Failed to validate login: " + e.getMessage(), Alert.AlertType.ERROR);
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
}