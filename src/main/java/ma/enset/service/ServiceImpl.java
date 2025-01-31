package ma.enset.service;

import ma.enset.dao.AttendanceDao;
import ma.enset.dao.AttendanceDaoImpl;
import ma.enset.dao.StudentDao;
import ma.enset.dao.StudentDaoImpl;
import ma.enset.entities.Attendance;
import ma.enset.entities.Student;

import java.util.List;

public class ServiceImpl implements Service {
    StudentDao studentDao = new StudentDaoImpl();
    AttendanceDao attendanceDao = new AttendanceDaoImpl();

    @Override
    public void addStudent(Student student) {
        studentDao.save(student);
    }

    @Override
    public void deleteStudent(int id) {
        studentDao.deleteById(id);
    }

    @Override
    public void updateStudent(Student student) {
        studentDao.update(student);
    }

    @Override
    public Student getStudent(int id) {
        return studentDao.findById(id);
    }

    @Override
    public List<Student> getAllStudents() {
        return studentDao.findAll();
    }

    @Override
    public void addAttendance(Attendance attendance) {
        attendanceDao.save(attendance);
    }

    @Override
    public void deleteAttendance(int id) {
        attendanceDao.deleteById(id);
    }


    @Override
    public void updateAttendance(Attendance attendance) {
        attendanceDao.update(attendance);
    }

    @Override
    public Attendance getAttendance(int id) {
        return attendanceDao.findById(id);
    }

    @Override
    public List<Attendance> getAllAttendances() {
        return attendanceDao.findAll();
    }
}