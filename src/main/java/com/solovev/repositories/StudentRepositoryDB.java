package com.solovev.repositories;

import com.solovev.model.Student;
import com.solovev.util.Constants;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class StudentRepositoryDB implements Repository<Student>, AutoCloseable {
    private Connection connection;

    public StudentRepositoryDB() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            this.connection = DriverManager.getConnection(Constants.DB_URL, Constants.USERNAME, Constants.PASSWORD);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<Student> getStudents() {
        String sql = "select * from students";
        ArrayList<Student> students = new ArrayList<>();
        try (PreparedStatement preparedStatement = this.connection.prepareStatement(sql)) {
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                Student student = new Student();
                student.setId(resultSet.getInt(1));
                student.setName(resultSet.getString(2));
                student.setAge(resultSet.getInt(3));
                student.setNum(resultSet.getInt(4));
                student.setSalary(resultSet.getDouble(5));

                students.add(student);
            }
        } catch (SQLException e) {
        }
        return students;
    }

    @Override
    public boolean add(Student student) {
        String sql = "insert into students(fio,age,num,salary) values (?,?,?,?)";
        try (PreparedStatement preparedStatement = this.connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setString(1, student.getName());
            preparedStatement.setInt(2, student.getAge());
            preparedStatement.setInt(3, (int) student.getNum()); // todo refactor student to int
            preparedStatement.setDouble(4, student.getSalary());

            int row = preparedStatement.executeUpdate();
            if (row <= 0) {
                return false;
            }

            try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                if (generatedKeys.next())
                    student.setId(generatedKeys.getInt(1));
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public Student delete(int elemId) {
        Student deletedStudent = takeData(elemId);
        if (deletedStudent != null) {
            String sql = "delete from students where students.id=?";
            try (PreparedStatement preparedStatement = this.connection.prepareStatement(sql)) {
                preparedStatement.setInt(1, elemId);
            } catch (SQLException ignored) {
            }
            return deletedStudent;
        }
    }

    @Override
    public Collection<Student> takeData() {
        String sql = "select * from students";
        ArrayList<Student> students = new ArrayList<>();
        try (PreparedStatement preparedStatement = this.connection.prepareStatement(sql)) {
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                Student student = new Student();
                student.setId(resultSet.getInt(1));
                student.setName(resultSet.getString(2));
                student.setAge(resultSet.getInt(3));
                student.setNum(resultSet.getInt(4));
                student.setSalary(resultSet.getDouble(5)); //to do refactor to double

                students.add(student);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return students;
    }

    @Override
    public Student takeData(int elemId) {
        String sql = "select * from students where students.id=?";
        try (PreparedStatement preparedStatement = this.connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, elemId);

            ResultSet resultSet = preparedStatement.executeQuery();
            if (!resultSet.next())
                return null;
            Student student = new Student();
            student.setId(resultSet.getInt(1));
            student.setName(resultSet.getString(2));
            student.setAge(resultSet.getInt(3));
            student.setNum(resultSet.getInt(4));
            student.setSalary(resultSet.getInt(5));

            return student;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean replace(Student student) {
        String sql = "update students set students.fio=?, students.age=?, students.num=?, students.salary=? where students.id=?";
        try (PreparedStatement preparedStatement = this.connection.prepareStatement(sql)) {
            preparedStatement.setString(1, student.getName());
            preparedStatement.setInt(2, student.getAge());
            preparedStatement.setInt(3, (int) student.getNum());
            preparedStatement.setDouble(4, student.getSalary());
            preparedStatement.setInt(5, student.getId());
            return preparedStatement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void save() {
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public int lastId() {
        return 0;
    }

    @Override
    public Collection<Student> clear() {
        return null;
    }

    @Override
    public void close() {
        if (this.connection != null)
            try {
                this.connection.close();
            } catch (SQLException ignored) {}
    }
    }
}
