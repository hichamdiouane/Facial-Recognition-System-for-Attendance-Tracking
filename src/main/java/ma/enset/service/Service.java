package ma.enset.service;

import ma.enset.entities.Attendance;
import ma.enset.entities.Student;

import java.util.List;

public interface Service {
    // Student services
    void addStudent(Student student);
    void deleteStudent(int id);
    void updateStudent(Student student);
    Student getStudent(int id);
    List<Student> getAllStudents();

    // Attendance services
    void addAttendance(Attendance attendance);
    void deleteAttendance(int id);
    void updateAttendance(Attendance attendance);
    Attendance getAttendance(int id);
    List<Attendance> getAllAttendances();
}
