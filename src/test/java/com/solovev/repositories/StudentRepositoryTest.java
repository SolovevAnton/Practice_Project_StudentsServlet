package com.solovev.repositories;

import com.solovev.model.Student;
import com.solovev.util.Constants;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

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

    @BeforeEach
    @AfterEach
    /**
     * Method tests that database stays in its initial state after every test,
     * and fails if its not, so it can be seen and recovered quickly
     */
    void dbTest() throws ClassNotFoundException, SQLException {
        Class.forName("com.mysql.cj.jdbc.Driver");
        String sql = "SELECT * FROM students";
        try (
                PreparedStatement preparedStatement =
                        DriverManager
                                .getConnection(Constants.DB_URL, Constants.USERNAME, Constants.PASSWORD)
                                .prepareStatement(sql)) {
           // assertEquals();
        }
    }

    Collection<Student> studentsInDb = List.of(
            new Student(1, "Igor", 22, 100, 1200.00),
            new Student(2, "Ivan", 21, 101, 1300.00),
            new Student(3, "Ila", 23, 102, 1400.00),
            new Student(4, "Dima", 24, 103, 1500.00)
    );

    @Test
    void add() {
    }

    @Test
    void delete() {
    }

    @Test
    void replace() {
    }

    @Test
    void size() {
    }

    @Test
    void lastId() {
    }
}