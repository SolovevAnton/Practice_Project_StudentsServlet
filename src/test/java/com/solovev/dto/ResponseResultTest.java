package com.solovev.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.solovev.model.Student;
import com.solovev.repositories.Repository;
import com.solovev.repositories.StudentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ResponseResultTest {
    @Test
    public void serializationTest() throws JsonProcessingException {
        assertEquals("{}",respToString(empty));
        assertEquals("{\"message\":\"only\"}",respToString(onlyMsg));
        assertEquals("{\"data\":1}",respToString(onlyData));
        assertEquals("{\"message\":\"one\",\"data\":1}",respToString(full));
    }

    @Test
    void jsonToString() throws JsonProcessingException {
        assertEquals(empty.jsonToString(),respToString(empty));
        assertEquals(onlyMsg.jsonToString(),respToString(onlyMsg));
        assertEquals(onlyData.jsonToString(),respToString(onlyData));
        assertEquals(full.jsonToString(),respToString(full));
    }
    @Test
    void gettingDataInt() throws IOException {
        ResponseResult<Student> result = new ResponseResult<>();
        result.setData(new Student());
        assertEquals(new Student(),result.getData());


    }
    @ParameterizedTest
    @NullSource
    public void serializationNullTest(ResponseResult resp) throws JsonProcessingException {
        assertEquals("null",respToString(resp));
    }
    private final ObjectMapper objectMapper = new ObjectMapper();
    private ResponseResult<Integer> empty = new ResponseResult<>();
    private ResponseResult<Integer> onlyMsg = new ResponseResult<>("only");
    private ResponseResult<Integer> onlyData = new ResponseResult<>(1);
    private ResponseResult<Integer> full = new ResponseResult<>("one",1);

    /**
     * method converts result to string with object mapper to test serialization
     * @param responseResult result to convert
     * @return string
     */
    private <T> String respToString(ResponseResult<T> responseResult) throws JsonProcessingException {
        return objectMapper.writeValueAsString(responseResult);
    }


}