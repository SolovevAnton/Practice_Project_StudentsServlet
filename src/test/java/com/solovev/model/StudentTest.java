package com.solovev.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StudentTest {

    @Test
    void testEquals() {
        assertEquals(new Student(),new Student());
        Student id4= new Student();
        id4.setId(4);

        Student id4_copy= new Student();
        id4.setId(4);

        assertEquals(id4,id4_copy);
    }
}