package com.solovev.repositories;

import com.solovev.model.Student;
import com.solovev.util.Constants;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * All tests for now in DB used real DB!!
 */
class StudentRepositoryTest {

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4})
    void takeData(int studentId) {
        try (StudentRepository repo = new StudentRepository()) {
            assertEquals(studentsInDb.toArray(new Student[0])[studentId - 1], // -1 since id starts from 1
                    repo.takeData(studentId));
        }

    }

    @Test
    void testTakeDataAll() {
        try (StudentRepository repo = new StudentRepository()) {
            assertEquals(studentsInDb, repo.takeData());
        }

    }

    @Test
    void addAndDeleteTestNormal() throws ClassNotFoundException, SQLException {
        Class.forName("com.mysql.cj.jdbc.Driver");
        String findStas = "SELECT * FROM students WHERE fio='Stas'";
        Student studentToAdd = new Student(4, "Stas", 25, 104, 1600.00);

        try (PreparedStatement preparedStatement =
                     DriverManager
                             .getConnection(Constants.DB_URL, Constants.USERNAME, Constants.PASSWORD)
                             .prepareStatement(findStas)) {
            //checks that base does not contain Stas
            Assumptions.assumeFalse(preparedStatement.executeQuery().next());

            try (StudentRepository repo = new StudentRepository()) {
                //added test
                repo.add(studentToAdd);
                ResultSet found = preparedStatement.executeQuery();
                found.next();
                Student studentInDb = studentFactory(found);

                assertEquals(studentInDb, studentToAdd);

                //delete test
                Student deletedStudent = repo.delete(studentInDb.getId());
                found = preparedStatement.executeQuery();

                assertEquals(studentToAdd,deletedStudent);
                assertFalse(found.next());
            }
        }
    }

    @Test
    void replace() throws ClassNotFoundException, SQLException {
        Class.forName("com.mysql.cj.jdbc.Driver");
        String findStas = "SELECT * FROM students WHERE fio='Stas'";
        Student studentToReplaceWith = new Student(-1, "Stas", 25, 104, 1600.00);

        try (PreparedStatement preparedStatement =
                     DriverManager
                             .getConnection(Constants.DB_URL, Constants.USERNAME, Constants.PASSWORD)
                             .prepareStatement(findStas)) {
            //checks that base does not contain Stas
            Assumptions.assumeFalse(preparedStatement.executeQuery().next());
        }

        fail();
    }

    @Test
    void size() {
        fail();
    }

    @Test
    void lastId() {
        fail();
    }
    @BeforeEach
    @AfterEach
    /**
     * Method tests that database stays in its initial state after every test,
     * and fails if its not, so it can be seen and recovered quickly
     */
    void dbTest() throws ClassNotFoundException, SQLException {
        Class.forName("com.mysql.cj.jdbc.Driver");
        String sql = "SELECT * FROM students";
        Collection<Student> students = new ArrayList<>();
        try (
                PreparedStatement preparedStatement =
                        DriverManager
                                .getConnection(Constants.DB_URL, Constants.USERNAME, Constants.PASSWORD)
                                .prepareStatement(sql)) {
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                students.add(studentFactory(resultSet));
            }
        }
        //checks if initial collection equals the real one if not throws
        if (!students.equals(studentsInDb)) {
            throw new RuntimeException(
                    String.format("Collections does not match\nexpected: %s \nfound:    %s",
                            studentsInDb,
                            students));
        }
    }

    /**
     * Builds student based on this result set
     *
     * @param resultSet to create student from
     * @return created student
     */
    private Student studentFactory(ResultSet resultSet) throws SQLException {
        Student student = new Student();
        student.setId(resultSet.getInt(1));
        student.setName(resultSet.getString(2));
        student.setAge(resultSet.getInt(3));
        student.setNum(resultSet.getInt(4));
        student.setSalary(resultSet.getDouble(5));
        return student;
    }

    private Collection<Student> studentsInDb = List.of(
            new Student(1, "Igor", 22, 100, 1200.00),
            new Student(2, "Ivan", 21, 101, 1300.00),
            new Student(3, "Ila", 23, 102, 1400.00),
            new Student(4, "Dima", 24, 103, 1500.00)
    );
}