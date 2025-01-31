package ma.enset.app;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import org.opencv.objdetect.CascadeClassifier;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ImageCaptureInterface {
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }
    private final Integer professorId; // Ajout de l'ID du professeur
    private final VBox content;
    private final ImageView imageView;
    private VideoCapture capture;
    private boolean cameraActive;
    private final Label resultLabel;
    private final OkHttpClient httpClient;
    private static final String API_URL = "http://127.0.0.1:8000/recognize/";
    private ScheduledExecutorService timer;
    private CascadeClassifier faceDetector;
    private static final String CSV_PATH = "attendance.csv";
    private final Set<String> todayAttendance;
    private final String today;
    private ComboBox<FieldItem> fieldComboBox;
    private ComboBox<SubjectItem> subjectComboBox;
    private Integer selectedFieldId;
    private Integer selectedSubjectId;
    private Integer currentAttendanceId;
    private static final String DB_URL = "jdbc:sqlite:D:/ENSET/S3/Java/Project/back-end/attendance_system.db";

    private static class FieldItem {
        private final int id;
        private final String name;

        public FieldItem(int id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private static class SubjectItem {
        private final int id;
        private final String name;

        public SubjectItem(int id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public ImageCaptureInterface(Integer professorId) {
        this.professorId = professorId;
        today = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        todayAttendance = new HashSet<>();
        loadTodayAttendance();

        faceDetector = new CascadeClassifier("haarcascade_frontalface_default.xml");

        httpClient = new OkHttpClient();
        content = new VBox(10);
        content.setPadding(new Insets(20));

        Label titleLabel = new Label("Face Recognition Attendance System");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        GridPane selectionGrid = new GridPane();
        selectionGrid.setHgap(10);
        selectionGrid.setVgap(10);
        selectionGrid.setPadding(new Insets(10));

        Label fieldLabel = new Label("Field:");
        fieldComboBox = new ComboBox<>();
        selectionGrid.add(fieldLabel, 0, 0);
        selectionGrid.add(fieldComboBox, 1, 0);

        Label subjectLabel = new Label("Subject:");
        subjectComboBox = new ComboBox<>();
        selectionGrid.add(subjectLabel, 0, 1);
        selectionGrid.add(subjectComboBox, 1, 1);

        loadFields();
        fieldComboBox.setOnAction(e -> {
            FieldItem selectedField = fieldComboBox.getSelectionModel().getSelectedItem();
            if (selectedField != null) {
                selectedFieldId = selectedField.id;
                loadSubjects();
            }
        });

        subjectComboBox.setOnAction(e -> {
            SubjectItem selectedSubject = subjectComboBox.getSelectionModel().getSelectedItem();
            if (selectedSubject != null) {
                selectedSubjectId = selectedSubject.id;
                loadTodayAttendance();
            }
        });

        imageView = new ImageView();
        imageView.setFitWidth(400);
        imageView.setFitHeight(300);
        imageView.setPreserveRatio(true);

        Button startButton = new Button("Start Capture");
        Button stopButton = new Button("Stop Capture");
        stopButton.setDisable(true);

        resultLabel = new Label("Select a field and subject to begin");

        startButton.setOnAction(e -> {
            if (selectedFieldId == null || selectedSubjectId == null) {
                resultLabel.setText("Please select both field and subject first");
                return;
            }
            startCameraWithRecognition();
            startButton.setDisable(true);
            stopButton.setDisable(false);
            fieldComboBox.setDisable(true);
            subjectComboBox.setDisable(true);
        });

        stopButton.setOnAction(e -> {
            stopCamera();
            startButton.setDisable(false);
            stopButton.setDisable(true);
            fieldComboBox.setDisable(false);
            subjectComboBox.setDisable(false);
        });

        content.getChildren().addAll(
                titleLabel,
                selectionGrid,
                imageView,
                startButton,
                stopButton,
                resultLabel
        );

        createCsvIfNotExists();
    }

    private void loadFields() {
        fieldComboBox.getItems().clear();
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(
                     "SELECT DISTINCT f.id, f.name " +
                             "FROM fields f " +
                             "JOIN professor_fields pf ON f.id = pf.field_id " +
                             "WHERE pf.professor_id = ? " +
                             "ORDER BY f.name")) {

            pstmt.setInt(1, professorId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    fieldComboBox.getItems().add(new FieldItem(
                            rs.getInt("id"),
                            rs.getString("name")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            resultLabel.setText("Error loading fields: " + e.getMessage());
        }
    }

    private void loadSubjects() {
        subjectComboBox.getItems().clear();
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(
                     "SELECT DISTINCT s.id, s.name " +
                             "FROM subjects s " +
                             "JOIN professor_subjects ps ON s.id = ps.subject_id " +
                             "WHERE ps.professor_id = ? " +
                             "ORDER BY s.name")) {

            pstmt.setInt(1, professorId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    subjectComboBox.getItems().add(new SubjectItem(
                            rs.getInt("id"),
                            rs.getString("name")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            resultLabel.setText("Error loading subjects: " + e.getMessage());
        }
    }

    private void loadTodayAttendance() {
        todayAttendance.clear();
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(
                     "SELECT sa.student_id " +
                             "FROM attendance a " +
                             "JOIN student_attendance sa ON a.id = sa.attendance_id " +
                             "WHERE a.date = ? AND a.subject_id = ?")) {

            pstmt.setString(1, today);
            pstmt.setInt(2, selectedSubjectId != null ? selectedSubjectId : -1);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    todayAttendance.add(rs.getInt("student_id") + "_" + selectedSubjectId);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createCsvIfNotExists() {
        try {
            File csvFile = new File(CSV_PATH);
            if (!csvFile.exists()) {
                try (FileWriter writer = new FileWriter(csvFile)) {
                    writer.write("DateTime,StudentName,SubjectID,Field\n");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendImageToServer(File imageFile, Path tempFile) {
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("image", imageFile.getName(),
                        RequestBody.create(MediaType.parse("image/jpeg"), imageFile))
                .build();

        Request request = new Request.Builder()
                .url(API_URL)
                .post(requestBody)
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                cleanupTempFile(tempFile);
                Platform.runLater(() -> {
                    resultLabel.setText("Failed to process image");
                    resultLabel.setStyle("-fx-text-fill: red; -fx-font-size: 16px;");
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try (ResponseBody responseBody = response.body()) {
                    if (response.isSuccessful() && responseBody != null) {
                        String name = responseBody.string().trim();
                        System.out.println("Recognized student: " + name);

                        Integer studentId = getStudentIdByName(name);
                        if (studentId == null) {
                            Platform.runLater(() -> {
                                resultLabel.setText(name + " (Unknown student)");
                                resultLabel.setStyle("-fx-text-fill: red; -fx-font-size: 16px;");
                            });
                            return;
                        }

                        // Check if student belongs to the selected field
                        if (!verifyStudentField(studentId, selectedFieldId)) {
                            Platform.runLater(() -> {
                                resultLabel.setText(name + " (Not enrolled in this field)");
                                resultLabel.setStyle("-fx-text-fill: red; -fx-font-size: 16px;");
                            });
                            return;
                        }

                        // Record attendance
                        AttendanceResult result = recordAttendance(studentId, name);
                        Platform.runLater(() -> {
                            resultLabel.setText(result.message);
                            resultLabel.setStyle(result.style);
                        });
                    }
                } finally {
                    cleanupTempFile(tempFile);
                }
            }
        });
    }

    private static class AttendanceResult {
        final String message;
        final String style;

        AttendanceResult(String message, String style) {
            this.message = message;
            this.style = style;
        }
    }

    private AttendanceResult recordAttendance(int studentId, String studentName) {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            // Start transaction
            conn.setAutoCommit(false);
            try {
                // Check if we already have an attendance session for today
                if (currentAttendanceId == null) {
                    currentAttendanceId = createOrGetAttendanceSession(conn);
                    if (currentAttendanceId == null) {
                        throw new SQLException("Failed to create or get attendance session");
                    }
                }

                // Check if student already marked as present today
                if (isStudentAlreadyPresent(conn, currentAttendanceId, studentId)) {
                    return new AttendanceResult(
                            studentName + " (Already marked present)",
                            "-fx-text-fill: blue; -fx-font-size: 16px;"
                    );
                }

                // Insert new attendance record
                insertStudentAttendance(conn, currentAttendanceId, studentId);

                // Log attendance in CSV (optional, you can remove if not needed)
                logAttendanceToCSV(studentName, selectedSubjectId, getFieldName(selectedFieldId));

                conn.commit();
                return new AttendanceResult(
                        studentName + " (Attendance recorded successfully)",
                        "-fx-text-fill: green; -fx-font-size: 16px;"
                );

            } catch (Exception e) {
                conn.rollback();
                e.printStackTrace();
                return new AttendanceResult(
                        "Error recording attendance: " + e.getMessage(),
                        "-fx-text-fill: red; -fx-font-size: 16px;"
                );
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return new AttendanceResult(
                    "Database error: " + e.getMessage(),
                    "-fx-text-fill: red; -fx-font-size: 16px;"
            );
        }
    }

    private Integer createOrGetAttendanceSession(Connection conn) throws SQLException {
        // Check for existing session
        String checkQuery = """
            SELECT id FROM attendance 
            WHERE date = ? AND subject_id = ? AND professor_id = ?
            """;

        try (PreparedStatement pstmt = conn.prepareStatement(checkQuery)) {
            pstmt.setString(1, today);
            pstmt.setInt(2, selectedSubjectId);
            pstmt.setInt(3, professorId);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        }

        // Create new session
        String insertQuery = """
            INSERT INTO attendance (subject_id, professor_id, date, status) 
            VALUES (?, ?, ?, 'PRESENT')
            """;

        try (PreparedStatement pstmt = conn.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, selectedSubjectId);
            pstmt.setInt(2, professorId);
            pstmt.setString(3, today);

            pstmt.executeUpdate();
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
        }

        return null;
    }

    private boolean isStudentAlreadyPresent(Connection conn, int attendanceId, int studentId) throws SQLException {
        String query = """
            SELECT 1 FROM student_attendance 
            WHERE attendance_id = ? AND student_id = ?
            """;

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, attendanceId);
            pstmt.setInt(2, studentId);

            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        }
    }

    private void insertStudentAttendance(Connection conn, int attendanceId, int studentId) throws SQLException {
        String query = """
            INSERT INTO student_attendance (attendance_id, student_id, timestamp) 
            VALUES (?, ?, CURRENT_TIMESTAMP)
            """;

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, attendanceId);
            pstmt.setInt(2, studentId);
            pstmt.executeUpdate();
        }
    }

    private String getFieldName(int fieldId) {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement("SELECT name FROM fields WHERE id = ?")) {
            pstmt.setInt(1, fieldId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("name");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "Unknown Field";
    }

    private void logAttendanceToCSV(String studentName, int subjectId, String fieldName) {
        try (FileWriter fw = new FileWriter(CSV_PATH, true);
             BufferedWriter bw = new BufferedWriter(fw)) {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            bw.write(String.format("%s,%s,%d,%s%n", timestamp, studentName, subjectId, fieldName));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean insertStudentAttendance(int studentId) {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            // Ensure we have an attendance record
            if (currentAttendanceId == null) {
                currentAttendanceId = createOrGetAttendanceRecord();
                if (currentAttendanceId == null) {
                    System.out.println("Failed to create/get attendance record");
                    return false;
                }
            }

            // Check if student attendance already exists
            String checkQuery = """
                SELECT 1 FROM student_attendance 
                WHERE attendance_id = ? AND student_id = ?
                """;

            try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
                checkStmt.setInt(1, currentAttendanceId);
                checkStmt.setInt(2, studentId);

                ResultSet rs = checkStmt.executeQuery();
                if (rs.next()) {
                    System.out.println("Student attendance already recorded");
                    return false;
                }
            }

            // Insert new student attendance record
            String insertQuery = """
                INSERT INTO student_attendance (attendance_id, student_id) 
                VALUES (?, ?)
                """;

            try (PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {
                insertStmt.setInt(1, currentAttendanceId);
                insertStmt.setInt(2, studentId);

                int result = insertStmt.executeUpdate();
                System.out.println("Student attendance inserted: " + (result > 0));
                return result > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error inserting student attendance: " + e.getMessage());
            return false;
        }
    }

    private boolean verifyStudentField(int studentId, int fieldId) {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(
                     "SELECT 1 FROM students WHERE id = ? AND field_id = ?")) {

            pstmt.setInt(1, studentId);
            pstmt.setInt(2, fieldId);

            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private Integer getStudentIdByName(String name) {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(
                     "SELECT id FROM students WHERE name = ?")) {

            pstmt.setString(1, name);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Integer createOrGetAttendanceRecord() {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            System.out.println("Creating or fetching attendance record...");
            System.out.println("Today: " + today + ", SubjectId: " + selectedSubjectId + ", ProfessorId: " + professorId);

            String checkQuery = "SELECT id FROM attendance WHERE date = ? AND subject_id = ? AND professor_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(checkQuery)) {
                pstmt.setString(1, today);
                pstmt.setInt(2, selectedSubjectId);
                pstmt.setInt(3, professorId);

                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    int existingId = rs.getInt("id");
                    System.out.println("Attendance record already exists with ID: " + existingId);
                    return existingId;
                }
            }

            System.out.println("No existing attendance record found. Creating new...");
            String insertQuery = "INSERT INTO attendance (subject_id, professor_id, date, status) VALUES (?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setInt(1, selectedSubjectId);
                pstmt.setInt(2, professorId);
                pstmt.setString(3, today);
                pstmt.setString(4, "PRESENT");

                int affectedRows = pstmt.executeUpdate();
                if (affectedRows > 0) {
                    ResultSet rs = pstmt.getGeneratedKeys();
                    if (rs.next()) {
                        int newId = rs.getInt(1);
                        System.out.println("Created new attendance record with ID: " + newId);
                        return newId;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error creating attendance record: " + e.getMessage());
        }
        return null;
    }

    public VBox getContent() {
        return content;
    }

    private void processFrame() {
        if (!cameraActive) return;

        Mat frame = new Mat();
        if (capture.read(frame)) {
            Mat gray = new Mat();
            Imgproc.cvtColor(frame, gray, Imgproc.COLOR_BGR2GRAY);

            MatOfRect faces = new MatOfRect();
            faceDetector.detectMultiScale(gray, faces);

            for (Rect rect : faces.toArray()) {
                Imgproc.rectangle(
                        frame,
                        new Point(rect.x, rect.y),
                        new Point(rect.x + rect.width, rect.y + rect.height),
                        new Scalar(0, 255, 255),
                        2
                );
            }

            Image imageToShow = matToImage(frame);
            Platform.runLater(() -> imageView.setImage(imageToShow));

            if (faces.toArray().length > 0) {
                try {
                    Path tempFile = Files.createTempFile("frame", ".jpg");
                    Imgcodecs.imwrite(tempFile.toString(), frame);
                    sendImageToServer(tempFile.toFile(), tempFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void startCameraWithRecognition() {
        capture = new VideoCapture(0);
        cameraActive = true;

        if (capture.isOpened()) {
            timer = Executors.newSingleThreadScheduledExecutor();
            timer.scheduleAtFixedRate(this::processFrame, 0, 100, TimeUnit.MILLISECONDS);
        }
    }

    public void stopCamera() {
        cameraActive = false;
        if (timer != null) {
            timer.shutdown();
            try {
                timer.awaitTermination(1, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        if (capture != null) {
            capture.release();
        }
        imageView.setImage(null);
        resultLabel.setText("Camera stopped");
    }

    private void cleanupTempFile(Path tempFile) {
        try {
            Files.delete(tempFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Image matToImage(Mat frame) {
        MatOfByte buffer = new MatOfByte();
        Imgcodecs.imencode(".jpg", frame, buffer);
        return new Image(new ByteArrayInputStream(buffer.toArray()));
    }
}