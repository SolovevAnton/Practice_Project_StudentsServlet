package com.solovev.repositories;

import com.solovev.model.Student;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class StudentRepositoryTest {

    @Test
    void add() {
        assertTrue(repo.add(emptyStudent));
        assertEquals(0,emptyStudent.getId());

        assertTrue(repo.add(firstStudent));
        assertFalse(repo.add(firstStudent));
        assertEquals(1,firstStudent.getId());

        assertTrue(repo.add(thirdStudent));
        assertEquals(2,thirdStudent.getId());
    }

    @Test
    void delete() {
    }

    @Test
    void takeData() {
    }

    @Test
    void testTakeData() {
    }

    @Test
    void replace() {
    }
    private Repository<Student> repo;
    private Student emptyStudent;
    private Student firstStudent;
    private Student secondStudent;
    private Student thirdStudent;
    /**
     * Initialise and reload repo
     */
    @BeforeEach
    private void initialize() throws IOException {
        Path pathToEmptyStudents = Path.of("src/test/resources/emptyRepo.json");
        String emptyContent = "[]";
        try(FileWriter fileWriter = new FileWriter(pathToEmptyStudents.toFile())){
            fileWriter.write(emptyContent);
        }
        repo = new StudentRepository(pathToEmptyStudents.toFile());
        emptyStudent = new Student();
        firstStudent = new Student(1,"first",20,1,1);
        secondStudent = new Student(2,"second",21,2,2);
        thirdStudent = new Student(3,"third",22,3,3);
    }
}