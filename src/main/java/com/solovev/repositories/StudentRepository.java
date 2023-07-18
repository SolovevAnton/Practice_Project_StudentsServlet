package com.solovev.repositories;

import com.fasterxml.jackson.core.type.TypeReference;
import com.solovev.util.Constants;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class StudentRepository extends AbstractRepository{

    public StudentRepository(File file) throws IOException {
        super(file);
    }
}
