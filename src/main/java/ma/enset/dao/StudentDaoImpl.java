package ma.enset.dao;

import ma.enset.entities.Student;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class StudentDaoImpl implements StudentDao {

    @Override
    public List<Student> findAll() {
        List<Student> students = new ArrayList<>();
        Connection connection = SingletonConnexionDB.getConnection();
        try {
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM students");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                byte[] embeddings = rs.getBytes("embeddings");
                Student student = new Student(name, embeddings);
                student.setId(id);
                students.add(student);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return students;
    }

    @Override
    public Student findById(Integer id) {
        Connection connection = SingletonConnexionDB.getConnection();
        try {
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM students WHERE id = ?");
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String name = rs.getString("name");
                byte[] embeddings = rs.getBytes("embeddings");
                Student student = new Student(name, embeddings);
                student.setId(id);
                return student;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void save(Student student) {
        Connection connection = SingletonConnexionDB.getConnection();
        try {
            PreparedStatement ps = connection.prepareStatement("INSERT INTO students (name, embeddings) VALUES (?, ?)");
            ps.setString(1, student.getName());
            ps.setBytes(2, student.getEmbeddings());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void update(Student student) {
        Connection connection = SingletonConnexionDB.getConnection();
        try {
            PreparedStatement ps = connection.prepareStatement("UPDATE students SET name = ?, embeddings = ? WHERE id = ?");
            ps.setString(1, student.getName());
            ps.setBytes(2, student.getEmbeddings());
            ps.setInt(3, student.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteById(Integer id) {
        Connection connection = SingletonConnexionDB.getConnection();
        try {
            PreparedStatement ps = connection.prepareStatement("DELETE FROM students WHERE id = ?");
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}