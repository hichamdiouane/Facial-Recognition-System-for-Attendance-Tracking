package ma.enset.app;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.geometry.Insets;

public class App extends Application {
    private LoginInterface loginInterface;
    private RegisterInterface registerInterface;
    private StudentManagementInterface userManagement;
    private ImageCaptureInterface imageCapture;
    private AttendanceStatistics logsAndStats;
    private Stage primaryStage;
    private Scene loginScene;
    private Scene registerScene;
    private Scene dashboardScene;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("Prof Dashboard");

        initializeScenes();

        primaryStage.setScene(loginScene);
        primaryStage.show();
    }

    private void initializeScenes() {
        // Initialize interfaces
        loginInterface = new LoginInterface(this::showMainDashboard, this::showRegister);
        registerInterface = new RegisterInterface(this::showLogin);

        // Create scenes
        loginScene = new Scene(loginInterface.getContent(), 400, 300);
        registerScene = new Scene(registerInterface.getContent(), 400, 400);

        // Add stylesheets
        loginScene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());
        registerScene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());
    }

    private void showMainDashboard(Stage stage) {
        if (dashboardScene == null) {
            // Create dashboard components
            TabPane tabPane = new TabPane();

            userManagement = new StudentManagementInterface();
            imageCapture = new ImageCaptureInterface(1);
            logsAndStats = new AttendanceStatistics("1");

            Tab userTab = new Tab("Student Management", userManagement.getContent());
            Tab imageTab = new Tab("Start Session", imageCapture.getContent());
            Tab logsTab = new Tab("Attendance Statistics", logsAndStats.getContent());

            userTab.setClosable(false);
            imageTab.setClosable(false);
            logsTab.setClosable(false);

            tabPane.getTabs().addAll(userTab, imageTab, logsTab);

            // Create header with welcome message and logout button
            VBox mainLayout = new VBox(10);
            HBox headerBox = new HBox(10);
            Label welcomeLabel = new Label("Welcome!");
            Button logoutButton = new Button("Logout");
            logoutButton.setOnAction(e -> handleLogout());

            headerBox.getChildren().addAll(welcomeLabel, logoutButton);
            headerBox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
            headerBox.setPadding(new Insets(10));

            mainLayout.getChildren().addAll(headerBox, tabPane);

            // Create dashboard scene
            dashboardScene = new Scene(mainLayout, 800, 720);
            dashboardScene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());
        }

        stage.setScene(dashboardScene);
        stage.setTitle("Professeur Dashboard");
        stage.centerOnScreen(); // Center the window after changing size
    }

    private void handleLogout() {
        // Clear any user session data if needed
        showLogin(primaryStage);
        // Reset dashboard scene to ensure fresh state on next login
        dashboardScene = null;
    }

    private void showLogin(Stage stage) {
        stage.setScene(loginScene);
        stage.setTitle("Prof Login");
        stage.centerOnScreen();
    }

    private void showRegister(Stage stage) {
        stage.setScene(registerScene);
        stage.setTitle("Prof Registration");
        stage.centerOnScreen();
    }

    @Override
    public void stop() {
        // Clean up resources when the application closes
        SingletonConnexionDB.closeConnection();
    }

    public static void main(String[] args) {
        launch(args);
    }
}