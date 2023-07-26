package com.solovev;

import com.solovev.model.Student;
import com.solovev.repositories.StudentRepository;
import com.solovev.util.Constants;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
         new StudentRepository().add(new Student());
    }
}