package com.solovev.repositories;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.solovev.model.Student;
import com.solovev.util.Constants;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class StudentRepository implements Repository<Student> {
    private ObjectMapper objectMapper = new ObjectMapper();
    private Set<Student> students = new HashSet<>();
    private File fileToStoreData;

    public StudentRepository() {
        Path fileToRead = Constants.STUDENTS_FILE;
        if (Files.exists(fileToRead)) {
            try {
                students = objectMapper.readValue(fileToRead.toFile(), new TypeReference<>() {
                });
                fileToStoreData = fileToRead.toFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public StudentRepository(File file) throws IOException {
        fileToStoreData = file;
        students = objectMapper.readValue(file, new TypeReference<>() {
        });
    }

    /**
     * Adds student to the repo;
     * Only unique students will be added
     * Note: it changes initial student ID!
     * @param elem student to add
     * @return true if student was successfully added, false otherwise;
     */
    @Override
    public boolean add(Student elem) {
        int maxId = students.stream().mapToInt(Student::getId).max().orElse(-1);
        boolean addSuccess = students.add(elem);
        if (addSuccess) {
            elem.setId(maxId + 1);
        }
        save();
        return addSuccess;
    }

    @Override
    public boolean delete(int elemId) {
        Optional<Student> foundStudent = students
                .stream()
                .filter(student -> student.getId() == elemId)
                .findFirst();

        foundStudent.ifPresent(
                student -> {
                    students.remove(student);
                    save();
                }
        );
        return foundStudent.isPresent();

    }

    @Override
    public Collection<Student> takeData() {
        return null;
    }

    @Override
    public Student takeData(int id) {
        return null;
    }

    @Override
    public boolean replace(Student newElem) {
        return false;
    }

    @Override
    public void save() {
        try {
            objectMapper.writeValue(fileToStoreData, students);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
