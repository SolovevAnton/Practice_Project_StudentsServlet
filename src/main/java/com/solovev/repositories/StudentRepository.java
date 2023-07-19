package com.solovev.repositories;

import com.solovev.model.Student;
import com.solovev.util.Constants;

import java.io.IOException;
import java.nio.file.Path;

public class StudentRepository extends AbstractRepository<Student> {

    public StudentRepository() throws IOException {
        this(Constants.STUDENTS_FILE);
    }

    public StudentRepository(Path path) throws IOException {
        super(path);
    }

}
