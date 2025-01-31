package ma.enset.dao;

import ma.enset.entities.Attendance;
import ma.enset.entities.Student;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class DaoTest {
    private static final StudentDao studentDao = new StudentDaoImpl();
    private static final AttendanceDao attendanceDao = new AttendanceDaoImpl();
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        while (true) {
            System.out.println("\n=== School Management System ===");
            System.out.println("=== Student Management ===");
            System.out.println("1. Add Student");
            System.out.println("2. View All Students");
            System.out.println("3. Find Student by ID");
            System.out.println("4. Update Student");
            System.out.println("5. Delete Student");
            System.out.println("\n=== Attendance Management ===");
            System.out.println("6. Record Attendance");
            System.out.println("7. View All Attendance Records");
            System.out.println("8. Find Attendance by ID");
            System.out.println("9. Update Attendance Record");
            System.out.println("10. Delete Attendance Record");
            System.out.println("\n0. Exit");
            System.out.print("Choose an option: ");

            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1: addStudent(); break;
                case 2: viewAllStudents(); break;
                case 3: findStudentById(); break;
                case 4: updateStudent(); break;
                case 5: deleteStudent(); break;
                case 6: recordAttendance(); break;
                case 7: viewAllAttendance(); break;
                case 8: findAttendanceById(); break;
                case 9: updateAttendance(); break;
                case 10: deleteAttendance(); break;
                case 0:
                    System.out.println("Exiting...");
                    SingletonConnexionDB.closeConnection();
                    return;
                default:
                    System.out.println("Invalid option!");
            }
        }
    }

    // Student Management Methods
    private static void addStudent() {
        System.out.print("Enter student name: ");
        String name = scanner.nextLine();
        byte[] embeddings = new byte[128];
        new Random().nextBytes(embeddings);
        Student newStudent = new Student(name, embeddings);
        studentDao.save(newStudent);
        System.out.println("Student added successfully!");
    }

    private static void viewAllStudents() {
        List<Student> students = studentDao.findAll();
        if (students.isEmpty()) {
            System.out.println("No students found.");
            return;
        }
        System.out.println("\nAll Students:");
        students.forEach(System.out::println);
    }

    private static void findStudentById() {
        System.out.print("Enter student ID: ");
        int id = scanner.nextInt();
        Student student = studentDao.findById(id);
        if (student != null) {
            System.out.println(student);
        } else {
            System.out.println("Student not found.");
        }
    }

    private static void updateStudent() {
        System.out.print("Enter student ID to update: ");
        int id = scanner.nextInt();
        scanner.nextLine();

        Student student = studentDao.findById(id);
        if (student == null) {
            System.out.println("Student not found.");
            return;
        }

        System.out.print("Enter new name: ");
        String newName = scanner.nextLine();
        student.setName(newName);

        byte[] newEmbeddings = new byte[128];
        new Random().nextBytes(newEmbeddings);
        student.setEmbeddings(newEmbeddings);

        studentDao.update(student);
        System.out.println("Student updated successfully!");
    }

    private static void deleteStudent() {
        System.out.print("Enter student ID to delete: ");
        int id = scanner.nextInt();
        studentDao.deleteById(id);
        System.out.println("Student deleted successfully!");
    }

    // Attendance Management Methods
    private static void recordAttendance() {
        System.out.print("Enter student ID: ");
        int studentId = scanner.nextInt();
        scanner.nextLine();

        System.out.print("Enter subject: ");
        String subject = scanner.nextLine();

        Attendance attendance = new Attendance(studentId, LocalDateTime.now(), subject);
        attendanceDao.save(attendance);
        System.out.println("Attendance recorded successfully!");
    }

    private static void viewAllAttendance() {
        List<Attendance> attendances = attendanceDao.findAll();
        if (attendances.isEmpty()) {
            System.out.println("No attendance records found.");
            return;
        }
        System.out.println("\nAll Attendance Records:");
        attendances.forEach(System.out::println);
    }

    private static void findAttendanceById() {
        System.out.print("Enter attendance ID: ");
        int id = scanner.nextInt();
        Attendance attendance = attendanceDao.findById(id);
        if (attendance != null) {
            System.out.println(attendance);
        } else {
            System.out.println("Attendance record not found.");
        }
    }

    private static void updateAttendance() {
        System.out.print("Enter attendance ID to update: ");
        int id = scanner.nextInt();
        scanner.nextLine();

        Attendance attendance = attendanceDao.findById(id);
        if (attendance == null) {
            System.out.println("Attendance record not found.");
            return;
        }

        System.out.print("Enter new student ID: ");
        int studentId = scanner.nextInt();
        scanner.nextLine();

        System.out.print("Enter new subject: ");
        String subject = scanner.nextLine();

        attendance.setStudentId(studentId);
        attendance.setSubject(subject);
        attendance.setTimestamp(LocalDateTime.now());

        attendanceDao.update(attendance);
        System.out.println("Attendance record updated successfully!");
    }

    private static void deleteAttendance() {
        System.out.print("Enter attendance ID to delete: ");
        int id = scanner.nextInt();
        attendanceDao.deleteById(id);
        System.out.println("Attendance record deleted successfully!");
    }
}