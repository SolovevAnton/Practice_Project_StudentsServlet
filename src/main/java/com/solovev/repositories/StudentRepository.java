package com.solovev.repositories;

import com.solovev.util.Constants;

import java.io.IOException;
import java.nio.file.Path;

public class StudentRepository extends AbstractRepository {

    public StudentRepository() throws IOException {
        super(Constants.STUDENTS_FILE);
    }

    public StudentRepository(Path path) throws IOException {
        super(path);
    }

}
