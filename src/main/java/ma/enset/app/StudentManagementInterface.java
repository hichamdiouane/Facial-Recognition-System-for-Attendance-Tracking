package ma.enset.app;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.application.Platform;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoCapture;
import java.io.ByteArrayInputStream;
import java.sql.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class StudentManagementInterface {
    private VBox content;
    private Connection connection;
    private VideoCapture capture;
    private ScheduledExecutorService timer;
    private ImageView cameraView;
    private Mat lastFrame;

    public StudentManagementInterface() {
        connectToDatabase();

        content = new VBox(10);
        content.setPadding(new Insets(20));
        content.getStyleClass().add("interface-content");

        Label titleLabel = new Label("Student Management");
        titleLabel.getStyleClass().add("title-label");

        // Camera preview
        cameraView = new ImageView();
        cameraView.setFitWidth(400);
        cameraView.setFitHeight(300);
        cameraView.setPreserveRatio(true);

        // Camera controls
        Button startCameraButton = new Button("Start Camera");
        startCameraButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        Button captureButton = new Button("Capture Photo");
        captureButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        HBox cameraControls = new HBox(10, startCameraButton, captureButton);

        // Student list
        ObservableList<String> students = FXCollections.observableArrayList();
        ListView<String> studentList = new ListView<>(students);
        studentList.setPrefHeight(200);
        loadStudents(students);

        // Search field
        TextField searchField = new TextField();
        searchField.setPromptText("Search students");
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            searchStudents(students, newValue);
        });

        // Student form
        GridPane studentForm = new GridPane();
        studentForm.setHgap(10);
        studentForm.setVgap(10);

        TextField nameField = new TextField();
        ComboBox<String> fieldDropdown = new ComboBox<>();
        TextField yearField = new TextField();

        fieldDropdown.setPromptText("Select a field");
        loadFields(fieldDropdown);

        studentForm.addRow(0, new Label("Name:"), nameField);
        studentForm.addRow(1, new Label("Field:"), fieldDropdown);
        studentForm.addRow(2, new Label("Year:"), yearField);

        // Camera control events
        startCameraButton.setOnAction(e -> {
            if (capture == null || !capture.isOpened()) {
                startCamera();
                startCameraButton.setText("Stop Camera");
            } else {
                stopCamera();
                startCameraButton.setText("Start Camera");
            }
        });

        captureButton.setOnAction(e -> lastFrame = captureFrame());

        // Buttons
        HBox buttonBox = new HBox(10);
        Button addButton = new Button("Add Student");
        addButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
        Button updateButton = new Button("Update Student");
        updateButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
        Button removeButton = new Button("Remove Student");
        removeButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");

        addButton.setOnAction(event -> {
            if (lastFrame != null) {
                String selectedField = fieldDropdown.getSelectionModel().getSelectedItem();
                if (selectedField != null) {
                    addStudent(nameField.getText(), selectedField, yearField.getText(), lastFrame);
                    loadStudents(students);
                    clearForm(nameField, fieldDropdown, yearField);
                } else {
                    showAlert("Error", "Please select a field!");
                }
            } else {
                showAlert("Error", "Please capture a photo first!");
            }
        });

        updateButton.setOnAction(event -> {
            String selectedStudent = studentList.getSelectionModel().getSelectedItem();
            if (selectedStudent != null) {
                String selectedField = fieldDropdown.getSelectionModel().getSelectedItem();
                if (selectedField != null) {
                    updateStudent(selectedStudent, nameField.getText(), selectedField, yearField.getText(), lastFrame);
                    loadStudents(students);
                    clearForm(nameField, fieldDropdown, yearField);
                } else {
                    showAlert("Error", "Please select a field!");
                }
            }
        });

        removeButton.setOnAction(event -> {
            String selectedStudent = studentList.getSelectionModel().getSelectedItem();
            if (selectedStudent != null) {
                removeStudent(selectedStudent);
                loadStudents(students);
                clearForm(nameField, fieldDropdown , yearField);
            }
        });

        buttonBox.getChildren().addAll(addButton, updateButton, removeButton);

        VBox cameraSection = new VBox(10, cameraView, cameraControls);
        HBox mainContent = new HBox(20, cameraSection, new VBox(10, searchField, studentList));

        content.getChildren().addAll(titleLabel, mainContent, studentForm, buttonBox);
    }

    private void clearForm(TextField nameField, ComboBox<String> fieldDropdown, TextField yearField) {
        nameField.clear();
        fieldDropdown.getSelectionModel().clearSelection();
        yearField.clear();
        lastFrame = null;
    }


    private void startCamera() {
        capture = new VideoCapture(0);
        timer = Executors.newSingleThreadScheduledExecutor();
        timer.scheduleAtFixedRate(() -> {
            Mat frame = new Mat();
            if (capture.read(frame)) {
                Image image = matToImage(frame);
                Platform.runLater(() -> cameraView.setImage(image));
            }
        }, 0, 33, TimeUnit.MILLISECONDS);
    }

    private void stopCamera() {
        if (timer != null && !timer.isShutdown()) {
            timer.shutdown();
            try {
                timer.awaitTermination(33, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                System.err.println("Exception in stopping camera: " + e.getMessage());
            }
        }
        if (capture != null && capture.isOpened()) {
            capture.release();
        }
        cameraView.setImage(null);
    }

    private Mat captureFrame() {
        Mat frame = new Mat();
        if (capture != null && capture.isOpened()) {
            capture.read(frame);
            return frame;
        }
        return null;
    }

    private Image matToImage(Mat frame) {
        try {
            MatOfByte buffer = new MatOfByte();
            Imgcodecs.imencode(".png", frame, buffer);
            return new Image(new ByteArrayInputStream(buffer.toArray()));
        } catch (Exception e) {
            System.err.println("Error converting Mat to Image: " + e.getMessage());
            return null;
        }
    }

    private byte[] matToBytes(Mat frame) {
        try {
            MatOfByte buffer = new MatOfByte();
            Imgcodecs.imencode(".png", frame, buffer);
            return buffer.toArray();
        } catch (Exception e) {
            System.err.println("Error converting Mat to bytes: " + e.getMessage());
            return null;
        }
    }

    private void connectToDatabase() {
        connection = SingletonConnexionDB.getConnection();
    }

    private void loadFields(ComboBox<String> fieldDropdown) {
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT name FROM fields")) {
            while (resultSet.next()) {
                fieldDropdown.getItems().add(resultSet.getString("name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load fields: " + e.getMessage());
        }
    }


    private void loadStudents(ObservableList<String> students) {
        students.clear();
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT name FROM students")) {
            while (resultSet.next()) {
                students.add(resultSet.getString("name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load students: " + e.getMessage());
        }
    }

    private Integer getFieldIdByName(String fieldName) {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT id FROM fields WHERE name = ?")) {
            statement.setString(1, fieldName);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("id");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to retrieve field ID: " + e.getMessage());
        }
        return null;
    }

    private void addStudent(String name, String fieldName, String year, Mat photo) {
        Integer fieldId = getFieldIdByName(fieldName);
        if (fieldId == null) {
            showAlert("Error", "Field not found: " + fieldName);
            return;
        }

        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO students (name, field_id, year, embeddings) VALUES (?, ?, ?, ?)")) {
            statement.setString(1, name);
            statement.setInt(2, fieldId);
            statement.setString(3, year);
            statement.setBytes(4, matToBytes(photo));
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to add student: " + e.getMessage());
        }
    }

    private void updateStudent(String oldName, String newName, String fieldName, String year, Mat photo) {
        Integer fieldId = getFieldIdByName(fieldName);
        if (fieldId == null) {
            showAlert("Error", "Field not found: " + fieldName);
            return;
        }

        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE students SET name = ?, field_id = ?, year = ?, embeddings = ? WHERE name = ?")) {
            statement.setString(1, newName);
            statement.setInt(2, fieldId);
            statement.setString(3, year);
            if (photo != null) {
                statement.setBytes(4, matToBytes(photo));
            } else {
                statement.setNull(4, Types.BLOB);
            }
            statement.setString(5, oldName);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to update student: " + e.getMessage());
        }
    }

    private void removeStudent(String name) {
        try (PreparedStatement statement = connection.prepareStatement(
                "DELETE FROM students WHERE name = ?")) {
            statement.setString(1, name);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to remove student: " + e.getMessage());
        }
    }

    private void searchStudents(ObservableList<String> students, String query) {
        students.clear();
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT name FROM students WHERE name LIKE ?")) {
            statement.setString(1, "%" + query + "%");
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    students.add(resultSet.getString("name"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public VBox getContent() {
        return content;
    }
}
