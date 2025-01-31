package ma.enset.dao;

import ma.enset.entities.Attendance;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AttendanceDaoImpl implements AttendanceDao {

    @Override
    public List<Attendance> findAll() {
        List<Attendance> attendances = new ArrayList<>();
        Connection connection = SingletonConnexionDB.getConnection();
        try {
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM attendance");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("id");
                int studentId = rs.getInt("student_id");
                LocalDateTime timestamp = rs.getTimestamp("timestamp").toLocalDateTime();
                String subject = rs.getString("subject");
                Attendance attendance = new Attendance(studentId, timestamp, subject);
                attendance.setId(id);
                attendances.add(attendance);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return attendances;
    }

    @Override
    public Attendance findById(Integer id) {
        Connection connection = SingletonConnexionDB.getConnection();
        try {
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM attendance WHERE id = ?");
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int studentId = rs.getInt("student_id");
                LocalDateTime timestamp = rs.getTimestamp("timestamp").toLocalDateTime();
                String subject = rs.getString("subject");
                Attendance attendance = new Attendance(studentId, timestamp, subject);
                attendance.setId(id);
                return attendance;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void save(Attendance attendance) {
        Connection connection = SingletonConnexionDB.getConnection();
        try {
            PreparedStatement ps = connection.prepareStatement("INSERT INTO attendance (student_id, timestamp, subject) VALUES (?, ?, ?)");
            ps.setInt(1, attendance.getStudentId());
            ps.setTimestamp(2, Timestamp.valueOf(attendance.getTimestamp()));
            ps.setString(3, attendance.getSubject());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteById(Integer id) {
        Connection connection = SingletonConnexionDB.getConnection();
        try {
            PreparedStatement ps = connection.prepareStatement("DELETE FROM attendance WHERE id = ?");
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void update(Attendance attendance) {
        Connection connection = SingletonConnexionDB.getConnection();
        try {
            PreparedStatement ps = connection.prepareStatement("UPDATE attendance SET student_id = ?, timestamp = ?, subject = ? WHERE id = ?");
            ps.setInt(1, attendance.getStudentId());
            ps.setTimestamp(2, Timestamp.valueOf(attendance.getTimestamp()));
            ps.setString(3, attendance.getSubject());
            ps.setInt(4, attendance.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}