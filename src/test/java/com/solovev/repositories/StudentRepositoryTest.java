package com.solovev.repositories;

import com.solovev.model.Student;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class StudentRepositoryTest {
    @Nested
    class RealRepositoryTest {
        @Test
        void creationTest() {
            assertAll(StudentRepository::new);
        }

        @Test
        void readAddingDeletingTest() throws IOException {
            Repository<Student> realRepo = new StudentRepository();
            Student studentToTempAdd = new Student();
            studentToTempAdd.setNum(new Random().nextLong()); // in very rare cases this test might get canceled due to this student already is presented in real repo

            Assumptions.assumeTrue(realRepo.add(studentToTempAdd));

            //finding data test
            assertSame(studentToTempAdd,realRepo.takeData(realRepo.lastId()));

            assertSame(studentToTempAdd,realRepo.delete(realRepo.lastId()));
        }
    }

    @ParameterizedTest
    @NullSource
    void nullTests(Student student) {
        assertThrows(NullPointerException.class, () -> repo.add(student));
        assertThrows(NullPointerException.class, () -> repo.replace(student));
    }

    @Test
    void add() {
        assertTrue(repo.add(emptyStudent));
        assertEquals(1, emptyStudent.getId());

        assertTrue(repo.add(firstStudent));
        assertFalse(repo.add(firstStudent));

        assertEquals(2, firstStudent.getId());

        assertTrue(repo.add(thirdStudent));
        assertEquals(3, thirdStudent.getId());
    }

    @Test
    void delete() {
        repo.add(emptyStudent);
        repo.add(firstStudent);

        assertEquals(emptyStudent,repo.delete(1));
        assertNull(repo.delete(emptyStudent.getId()));

        assertTrue(repo.add(emptyStudent));
        assertEquals(3, emptyStudent.getId());
    }

    @Test
    void takeData() {
        assertEquals(new HashSet<>(), repo.takeData());

        fillRepo();
        assertEquals(Set.of(emptyStudent, firstStudent, secondStudent, thirdStudent), repo.takeData());
    }

    @Test
    void intTakeData() {
        assertNull(repo.takeData(0));

        fillRepo();
        assertEquals(emptyStudent, repo.takeData(1));
        assertEquals(thirdStudent, repo.takeData(4));

        assertNull(repo.takeData(-1));
        assertNull(repo.takeData(5));
    }

    @Test
    void replace() {
        assertFalse(repo.replace(emptyStudent));

        fillRepo();

        Student emptyReplacement = new Student();

        assertTrue(repo.replace(emptyReplacement));
        assertTrue(repo.replace(emptyReplacement));

        assertEquals(emptyReplacement, repo.takeData(1));

        assertTrue(repo.replace(emptyStudent));

        emptyReplacement.setId(4);
        assertFalse(repo.replace(emptyReplacement));
        assertEquals(emptyStudent, repo.takeData(1));
    }

    @Test
    void creationFromSource() throws IOException {
        Repository<Student> repo = new StudentRepository(Path.of("src/test/resources/testStudentsData.json"));
    }
    @Test
    void gettingFromSource() throws IOException {
        Repository<Student> repo = new StudentRepository(Path.of("src/test/resources/testStudentsData.json"));
        assertEquals(new Student(),repo.takeData(0));
    }

    private AbstractRepository<Student> repo;
    private Student emptyStudent;
    private Student firstStudent;
    private Student secondStudent;
    private Student thirdStudent;

    /**
     * Initialise and reload repo
     */
    @BeforeEach
    public void initialize() throws IOException {
        Path pathToEmptyStudents = Path.of("src/test/resources/emptyStudentsRepo.json");
        String emptyContent = "[]";
        try (FileWriter fileWriter = new FileWriter(pathToEmptyStudents.toFile())) {
            fileWriter.write(emptyContent);
        }
        repo = new StudentRepository(pathToEmptyStudents);
        emptyStudent = new Student();
        firstStudent = new Student(1, "first", 20, 1, 1);
        secondStudent = new Student(2, "second", 21, 2, 2);
        thirdStudent = new Student(3, "third", 22, 3, 3);
    }

    /**
     * Fills repo with all the students starting from the empty one
     */
    private void fillRepo() {
        repo.add(emptyStudent);
        repo.add(firstStudent);
        repo.add(secondStudent);
        repo.add(thirdStudent);
    }
}