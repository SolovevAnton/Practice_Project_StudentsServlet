package com.solovev.repositories;

import com.solovev.model.Student;
import com.solovev.util.Constants;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;

public class StudentRepository implements Repository<Student>, AutoCloseable {
    private Connection connection;

    //todo how to test without real db??
    public StudentRepository() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            this.connection = DriverManager.getConnection(Constants.DB_URL, Constants.USERNAME, Constants.PASSWORD);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Builds student based on this result set
     *
     * @param resultSet to create student from
     * @return created student
     */
    private static Student studentFactory(ResultSet resultSet) throws SQLException {
        Student student = new Student();
        student.setId(resultSet.getInt(1));
        student.setName(resultSet.getString(2));
        student.setAge(resultSet.getInt(3));
        student.setNum(resultSet.getInt(4));
        student.setSalary(resultSet.getDouble(5));
        return student;
    }

    /**
     * Method helps to execute cuery with ONE result of single int and return it
     *
     * @param query to execute
     * @return int that got from DB or 0 if nothing
     */
    private int gettingOneLineIntFromDB(String query) {
        int maxId = 0;
        try (PreparedStatement preparedStatement = this.connection.prepareStatement(query)) {
            ResultSet result = preparedStatement.executeQuery();
            result.next();
            maxId = result.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return maxId;
    }

    @Override
    public boolean add(Student student) throws SQLException { //todo how to autodecrement?
        String sql = "insert into students(fio,age,num,salary) values (?,?,?,?)";
        try (PreparedStatement preparedStatement = this.connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setString(1, student.getName());
            preparedStatement.setInt(2, student.getAge());
            preparedStatement.setInt(3, student.getNum());
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
        }
    }

    @Override
    public Student delete(int elemId) {
        Student deletedStudent = takeData(elemId);
        if (deletedStudent != null) {
            String sql = "delete from students where students.id=?";
            try (PreparedStatement preparedStatement = this.connection.prepareStatement(sql)) {
                preparedStatement.setInt(1, elemId);
                preparedStatement.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return deletedStudent;
    }

    @Override
    public Collection<Student> takeData() {
        String sql = "select * from students";
        ArrayList<Student> students = new ArrayList<>();
        try (PreparedStatement preparedStatement = this.connection.prepareStatement(sql)) {
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                students.add(studentFactory(resultSet));
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
            if (!resultSet.next()) {
                return null;
            }
            return studentFactory(resultSet);
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
    public int size() {
        String query = "SELECT COUNT(id) FROM students";
        return gettingOneLineIntFromDB(query);
    }

    @Override
    public int lastId() {
        String query = "SELECT MAX(id) FROM students;";
        return gettingOneLineIntFromDB(query);
    }


    @Override
    public void close() {
        if (this.connection != null)
            try {
                this.connection.close();
            } catch (SQLException ignored) {
            }
    }
}

