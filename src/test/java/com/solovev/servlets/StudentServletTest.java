package com.solovev.servlets;

import com.solovev.dto.ResponseResult;
import com.solovev.model.Student;
import com.solovev.repositories.StudentRepository;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class StudentServletTest {
    @Nested
    public class doGetTest {
        @Test
        void doGetNotFound() throws IOException {
            when(request.getParameter("id")).thenReturn("-1");
            StringWriter stringWriter = new StringWriter();
            PrintWriter writer = new PrintWriter(stringWriter);
            when(response.getWriter()).thenReturn(writer);

            StudentServlet servlet = new StudentServlet();
            servlet.doGet(request, response);
            writer.flush();

            assertEquals("{\"message\":\"Student with this ID was not found\"}", stringWriter.toString().trim());
        }

        @Test
        void doGetNotInteger() throws IOException {
            when(request.getParameter("id")).thenReturn("1l");
            StringWriter stringWriter = new StringWriter();
            PrintWriter writer = new PrintWriter(stringWriter);
            when(response.getWriter()).thenReturn(writer);

            StudentServlet servlet = new StudentServlet();
            servlet.doGet(request, response);
            writer.flush();

            assertEquals("{\"message\":\"java.lang.NumberFormatException: For input string: \\\"1l\\\" Id must be an integer\"}", stringWriter.toString().trim());
        }

        @Test
        void doGetNormal() throws IOException {
            StudentRepository repo = new StudentRepository();
            Assumptions.assumeTrue(repo.takeData().size() > 0);

            Student existingStudent = repo.takeData().stream().findAny().get();

            when(request.getParameter("id")).thenReturn(String.valueOf(existingStudent.getId()));

            StringWriter stringWriter = new StringWriter();
            PrintWriter writer = new PrintWriter(stringWriter);
            when(response.getWriter()).thenReturn(writer);

            StudentServlet servlet = new StudentServlet();
            servlet.doGet(request, response);
            writer.flush();

            ResponseResult<Student> expectedResp = new ResponseResult<>(existingStudent);

            assertEquals(expectedResp.jsonToString(),stringWriter.toString());
        }
    }

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

}