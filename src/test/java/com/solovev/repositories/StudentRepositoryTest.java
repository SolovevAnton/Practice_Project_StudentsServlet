package com.solovev.repositories;

import com.solovev.model.Student;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class StudentRepositoryTest {
    @ParameterizedTest
    @NullSource
    void nullTests(Student student){
        assertThrows(NullPointerException.class,()->repo.add(student));
        assertThrows(NullPointerException.class,()->repo.replace(student));
    }

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
        repo.add(emptyStudent);
        repo.add(firstStudent);

        assertTrue(repo.delete(0));
        assertFalse(repo.delete(emptyStudent.getId()));
        assertTrue(repo.add(emptyStudent));
        assertEquals(2,emptyStudent.getId());
    }

    @Test
    void takeData() {
        assertEquals(new HashSet<>(),repo.takeData());

        fillRepo();
        assertEquals(Set.of(emptyStudent,firstStudent,secondStudent,thirdStudent),repo.takeData());
    }

    @Test
    void intTakeData() {
        assertNull(repo.takeData(0));

        fillRepo();
        assertEquals(emptyStudent,repo.takeData(0));
        assertEquals(thirdStudent,repo.takeData(3));

        assertNull(repo.takeData(-1));
        assertNull(repo.takeData(4));
    }

    @Test
    void replace() {
        assertFalse(repo.replace(emptyStudent));

        fillRepo();

        Student emptyReplacement = new Student();

        assertTrue(repo.replace(emptyReplacement));
        assertTrue(repo.replace(emptyReplacement));

        assertEquals(emptyReplacement,repo.takeData(0));

        assertTrue(repo.replace(emptyStudent));

        emptyReplacement.setId(4);
        assertFalse(repo.replace(emptyReplacement));
        assertEquals(emptyStudent,repo.takeData(0));
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

    /**
     * Fills repo with all the students starting from the empty one
     */
    private void fillRepo(){
        repo.add(emptyStudent);
        repo.add(firstStudent);
        repo.add(secondStudent);
        repo.add(thirdStudent);
    }
}