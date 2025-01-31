package ma.enset.app;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class AttendanceStatistics {
    private final Connection connection;
    private final String professorId;
    private final ComboBox<SubjectItem> subjectComboBox;
    private final DatePicker startDate;
    private final DatePicker endDate;
    private final TableView<AttendanceRecord> tableView;
    private final VBox content;
    private final Label attendanceStatisticsLabel;

    private record SubjectItem(int id, String name) {
        @Override
        public String toString() {
            return name;
        }
    }

    private record AttendanceRecord(
            String studentName,
            String subjectName,
            LocalDate date,
            String status
    ) {}

    public AttendanceStatistics(String professorId) {
        this.professorId = professorId;
        this.connection = SingletonConnexionDB.getConnection();

        content = new VBox(10);
        content.setPadding(new Insets(20));

        subjectComboBox = new ComboBox<>();
        startDate = new DatePicker(LocalDate.now().minusMonths(1));
        endDate = new DatePicker(LocalDate.now());

        tableView = new TableView<>();
        tableView.setPrefHeight(300);
        setupTableColumns();

        // Label to display attendance statistics
        attendanceStatisticsLabel = new Label("Attendance Statistics: N/A");

        content.getChildren().addAll(
                new Label("Subject:"), subjectComboBox,
                new Label("Start Date:"), startDate,
                new Label("End Date:"), endDate,
                tableView,
                attendanceStatisticsLabel
        );

        loadSubjects();
        setupFetchButton();
        setupExportButton();
    }

    private void setupTableColumns() {
        TableColumn<AttendanceRecord, String> studentCol = new TableColumn<>("Student");
        studentCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().studentName()));

        TableColumn<AttendanceRecord, String> subjectCol = new TableColumn<>("Subject");
        subjectCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().subjectName()));

        TableColumn<AttendanceRecord, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().date().format(DateTimeFormatter.ISO_LOCAL_DATE)));

        TableColumn<AttendanceRecord, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().status()));

        tableView.getColumns().addAll(studentCol, subjectCol, dateCol, statusCol);
    }

    private void loadSubjects() {
        try {
            String query = """
                SELECT s.id, s.name 
                FROM subjects s 
                JOIN professor_subjects ps ON s.id = ps.subject_id 
                WHERE ps.professor_id = ?
            """;

            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, professorId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                subjectComboBox.getItems().add(
                        new SubjectItem(rs.getInt("id"), rs.getString("name"))
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private List<AttendanceRecord> fetchRecords() {
        List<AttendanceRecord> records = new ArrayList<>();

        String query = """
            SELECT 
                s.name as student_name,
                sub.name as subject_name,
                a.date,
                a.status
            FROM attendance a
            JOIN student_attendance sa ON a.id = sa.attendance_id
            JOIN students s ON sa.student_id = s.id
            JOIN subjects sub ON a.subject_id = sub.id
            WHERE a.professor_id = ? 
            AND a.subject_id = ?
            AND a.date BETWEEN ? AND ?
            ORDER BY a.date DESC, s.name
        """;

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, professorId);
            stmt.setInt(2, subjectComboBox.getValue().id());
            stmt.setDate(3, Date.valueOf(startDate.getValue()));
            stmt.setDate(4, Date.valueOf(endDate.getValue()));

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                records.add(new AttendanceRecord(
                        rs.getString("student_name"),
                        rs.getString("subject_name"),
                        rs.getDate("date").toLocalDate(),
                        rs.getString("status")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Sample records (you can remove these after testing)
        records.add(new AttendanceRecord("EL MOUTAOUAKIL Abdellah", "UML", LocalDate.now(), "Absent"));
        records.add(new AttendanceRecord("Hafid ELMOUDEN", "UML", LocalDate.now(), "Absent"));
        records.add(new AttendanceRecord("Youssef", "UML", LocalDate.now(), "Absent"));
        records.add(new AttendanceRecord("DIOUANE Hicham", "UML", LocalDate.now(), "Present"));

        return records;
    }

    private void setupFetchButton() {
        Button fetchButton = new Button("View attendance");
        fetchButton.setOnAction(e -> {
            if (subjectComboBox.getValue() != null) {
                List<AttendanceRecord> records = fetchRecords();
                tableView.setItems(FXCollections.observableArrayList(records));
                updateAttendanceStatistics(records);
            } else {
                Alert alert = new Alert(Alert.AlertType.WARNING, "Please select a subject.");
                alert.showAndWait();
            }
        });
        content.getChildren().add(fetchButton);
    }

    // Calculate and update attendance statistics
    private void updateAttendanceStatistics(List<AttendanceRecord> records) {
        int totalStudents = records.size();
        int presentCount = 0;
        int absentCount = 0;

        for (AttendanceRecord record : records) {
            switch (record.status()) {
                case "Present" -> presentCount++;
                case "Absent" -> absentCount++;
            }
        }

        double presentPercentage = (totalStudents > 0) ? (double) presentCount / totalStudents * 100 : 0;
        double absentPercentage = (totalStudents > 0) ? (double) absentCount / totalStudents * 100 : 0;

        // Creating a formatted string for the statistics
        String statistics = String.format("""
            Total Students: %d
            Total Present: %d (%.2f%%)
            Total Absent: %d (%.2f%%)
            ------------------------------
            Present Percentage: %.2f%%
            Absent Percentage: %.2f%%
            """,
                totalStudents, presentCount, presentPercentage,
                absentCount, absentPercentage,
                presentPercentage, absentPercentage
        );

        // Update the statistics label with the formatted string
        attendanceStatisticsLabel.setText(statistics);

        // Optionally, you can add some styling to improve the appearance:
        attendanceStatisticsLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 10;");
    }

    // Function to export table data to a CSV file
    private void exportToCSV() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = fileChooser.showSaveDialog(null);
        if (file != null) {
            try (FileWriter writer = new FileWriter(file)) {
                // Write the header
                writer.append("Student Name,Subject Name,Date,Status\n");

                // Write the table data
                for (AttendanceRecord record : tableView.getItems()) {
                    writer.append(record.studentName()).append(",")
                            .append(record.subjectName()).append(",")
                            .append(record.date().format(DateTimeFormatter.ISO_LOCAL_DATE)).append(",")
                            .append(record.status()).append("\n");
                }

                // Success message
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Data exported successfully!");
                alert.showAndWait();
            } catch (IOException e) {
                e.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR, "Error exporting data.");
                alert.showAndWait();
            }
        }
    }

    // Set up the export button
    private void setupExportButton() {
        Button exportButton = new Button("Export to CSV");
        exportButton.setOnAction(e -> exportToCSV());
        content.getChildren().add(exportButton);
    }

    public VBox getContent() {
        return content;
    }
}
