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
        void readAddingDeletingTest() throws IOException {
            Student studentToTempAdd = new Student();
            studentToTempAdd.setNum(new Random().nextLong()); // in very rare cases this test might get canceled due to this student already is presented in real repo

            Assumptions.assumeTrue(realRepo.add(studentToTempAdd));

            //finding data test
            assertSame(studentToTempAdd,realRepo.takeData(realRepo.lastId()));

            assertSame(studentToTempAdd,realRepo.delete(realRepo.lastId()));
        }
        @Test
        void realReplaceTest() throws IOException {
            Student studentToTempAdd = new Student();
            studentToTempAdd.setNum(new Random().nextLong()); // in very rare cases this test might get canceled due to this student already is presented in real repo

            Assumptions.assumeTrue(realRepo.add(studentToTempAdd));

            Student studentToReplace = new Student();
            studentToReplace.setName("Replacement");
            studentToReplace.setId(studentToTempAdd.getId());

            Assumptions.assumeFalse(realRepo.takeData().contains(studentToReplace));

            assertTrue(realRepo.replace(studentToReplace));
            assertEquals(studentToReplace,realRepo.delete(studentToTempAdd.getId()));
        }
        @Test
        void realEqualsTest() throws IOException {
            Assumptions.assumeTrue(realRepo.size() > 0);

            for(Student existingStudent : realRepo.takeData()) {
                Student studentToCopy = new Student(existingStudent.getId(), existingStudent.getName(), existingStudent.getAge(), existingStudent.getNum(), existingStudent.getSalary());
                assertEquals(studentToCopy, new StudentRepository().takeData(existingStudent.getId()));
            }

        }
        private Repository<Student> realRepo;
        @BeforeEach
        private void reloadRepo() throws IOException {
            realRepo = new StudentRepository();
        }
    }
    @Test
    void equalsAfterDeserialization() throws IOException {
        testRepo.add(emptyStudent);
        Repository<Student> deserializedRepo = new StudentRepository(pathToEmptyStudents);
        assertEquals(emptyStudent,deserializedRepo.takeData(emptyStudent.getId()));
    }
    @ParameterizedTest
    @NullSource
    void nullTests(Student student) {
        assertThrows(NullPointerException.class, () -> testRepo.add(student));
        assertThrows(NullPointerException.class, () -> testRepo.replace(student));
    }

    @Test
    void add() {
        assertTrue(testRepo.add(emptyStudent));
        assertEquals(1, emptyStudent.getId());

        assertTrue(testRepo.add(firstStudent));
        assertFalse(testRepo.add(firstStudent));

        assertEquals(2, firstStudent.getId());

        assertTrue(testRepo.add(thirdStudent));
        assertEquals(3, thirdStudent.getId());
    }

    @Test
    void delete() {
        testRepo.add(emptyStudent);
        testRepo.add(firstStudent);

        assertEquals(emptyStudent, testRepo.delete(1));
        assertNull(testRepo.delete(emptyStudent.getId()));

        assertTrue(testRepo.add(emptyStudent));
        assertEquals(3, emptyStudent.getId());
    }

    @Test
    void takeData() {
        assertEquals(new HashSet<>(), testRepo.takeData());

        fillRepo();
        assertEquals(Set.of(emptyStudent, firstStudent, secondStudent, thirdStudent), testRepo.takeData());
    }

    @Test
    void intTakeData() {
        assertNull(testRepo.takeData(0));

        fillRepo();
        assertEquals(emptyStudent, testRepo.takeData(1));
        assertEquals(thirdStudent, testRepo.takeData(4));

        assertNull(testRepo.takeData(-1));
        assertNull(testRepo.takeData(5));
    }

    @Test
    void replace() {
        assertFalse(testRepo.replace(emptyStudent));

        fillRepo();

        Student emptyReplacement = new Student();

        assertTrue(testRepo.replace(emptyReplacement));
        assertTrue(testRepo.replace(emptyReplacement));

        assertEquals(emptyReplacement, testRepo.takeData(1));

        assertTrue(testRepo.replace(emptyStudent));

        emptyReplacement.setId(4);
        assertFalse(testRepo.replace(emptyReplacement));
        assertEquals(emptyStudent, testRepo.takeData(1));
    }

    @Test
    void creationFromSource() throws IOException {
        Repository<Student> repo = new StudentRepository(Path.of("src/test/resources/testStudentsData.json"));
    }
    @Test
    void gettingFromSource() throws IOException {
        Repository<Student> repo = new StudentRepository(Path.of("src/test/resources/testStudentsData.json"));
        assertEquals(firstStudent,repo.takeData(1));
    }

    private AbstractRepository<Student> testRepo;
    private Student emptyStudent;
    private Student firstStudent;
    private Student secondStudent;
    private Student thirdStudent;
    private Path pathToEmptyStudents = Path.of("src/test/resources/emptyStudentsRepo.json");
    /**
     * Initialise and reload repo
     */
    @BeforeEach
    public void initialize() throws IOException {
        String emptyContent = "[]";
        try (FileWriter fileWriter = new FileWriter(pathToEmptyStudents.toFile())) {
            fileWriter.write(emptyContent);
        }
        testRepo = new StudentRepository(pathToEmptyStudents);
        emptyStudent = new Student();
        firstStudent = new Student(1, "first", 20, 1, 1);
        secondStudent = new Student(2, "second", 21, 2, 2);
        thirdStudent = new Student(3, "third", 22, 3, 3);
    }

    /**
     * Fills repo with all the students starting from the empty one
     */
    private void fillRepo() {
        testRepo.add(emptyStudent);
        testRepo.add(firstStudent);
        testRepo.add(secondStudent);
        testRepo.add(thirdStudent);
    }
}