package com.solovev.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;

import static org.junit.jupiter.api.Assertions.*;

class ResponseResultTest {
    @Test
    public void serializationTest() throws JsonProcessingException {
        assertEquals("{}",respToString(empty));
        assertEquals("{\"message\":\"only\"}",respToString(onlyMsg));;
        assertEquals("{\"data\":1}",respToString(onlyData));;
        assertEquals("{\"message\":\"one\",\"data\":1}",respToString(full));
    }
    @ParameterizedTest
    @NullSource
    public void serializationNullTest(ResponseResult resp) throws JsonProcessingException {
        assertNull(respToString(resp));
    }
    private ObjectMapper objectMapper = new ObjectMapper();
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