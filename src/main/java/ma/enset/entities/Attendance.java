package ma.enset.entities;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Attendance implements Serializable {
    private int id;
    private int studentId;
    private LocalDateTime timestamp;
    private String subject;

    public Attendance(int studentId, String subject) {
        this.studentId = studentId;
        this.timestamp = LocalDateTime.now();
        this.subject = subject;
    }

    public Attendance(int studentId, LocalDateTime timestamp, String subject) {
        this.studentId = studentId;
        this.timestamp = timestamp;
        this.subject = subject;
    }

    // Getters
    public int getStudentId() { return studentId; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public String getSubject() { return subject; }
    public void setStudentId(int studentId) { this.studentId = studentId; }
    public void setSubject(String subject) { this.subject = subject; }
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    @Override
    public String toString() {
        return "Attendance{" +
                "id=" + id +
                ", studentId=" + studentId +
                ", timestamp=" + timestamp +
                ", subject='" + subject + '\'' +
                '}';
    }
}