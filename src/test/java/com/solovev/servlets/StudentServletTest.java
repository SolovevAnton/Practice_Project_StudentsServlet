package com.solovev.servlets;

import com.solovev.dto.ResponseResult;
import com.solovev.model.Student;
import com.solovev.repositories.Repository;
import com.solovev.repositories.StudentRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class StudentServletTest {
    @Nested
    class doPostTests {
        @Test
        void doPostExists() throws IOException, ServletException {
            StudentRepository repo = new StudentRepository();
            Assumptions.assumeTrue(repo.size() > 0);

            Student existingStudent = repo.takeData().stream().findAny().get();

            when(request.getParameter("name")).thenReturn(String.valueOf(existingStudent.getName()));
            when(request.getParameter("age")).thenReturn(String.valueOf(existingStudent.getAge()));
            when(request.getParameter("num")).thenReturn(String.valueOf(existingStudent.getNum()));
            when(request.getParameter("salary")).thenReturn(String.valueOf(existingStudent.getSalary()));

            StringWriter stringWriter = new StringWriter();
            PrintWriter writer = new PrintWriter(stringWriter);
            when(response.getWriter()).thenReturn(writer);

            StudentServlet servlet = new StudentServlet();
            servlet.doPost(request, response);
            writer.flush();

            assertEquals("{\"message\":\"This object already exists in database\"}", stringWriter.toString().trim());
        }

        @Test
        void doPostNotInteger() throws IOException, ServletException {
            when(request.getParameter("name")).thenReturn(null);
            when(request.getParameter("age")).thenReturn(null);
            when(request.getParameter("num")).thenReturn("1l");

            StringWriter stringWriter = new StringWriter();
            PrintWriter writer = new PrintWriter(stringWriter);
            when(response.getWriter()).thenReturn(writer);

            StudentServlet servlet = new StudentServlet();
            servlet.doPost(request, response);
            writer.flush();

            assertEquals("{\"message\":\"java.lang.NumberFormatException: For input string: \\\"1l\\\"\"}", stringWriter.toString().trim());
        }

        @Test
        void doPostMainScenario() throws IOException, ServletException {
            Assumptions.assumeFalse(new StudentRepository().takeData().contains(studentToAdd));

            when(request.getParameter("name")).thenReturn(studentToAdd.getName());
            when(request.getParameter("age")).thenReturn(String.valueOf(studentToAdd.getAge()));
            when(request.getParameter("num")).thenReturn(String.valueOf(studentToAdd.getNum()));
            when(request.getParameter("salary")).thenReturn(String.valueOf(studentToAdd.getSalary()));

            StringWriter stringWriter = new StringWriter();
            PrintWriter writer = new PrintWriter(stringWriter);
            when(response.getWriter()).thenReturn(writer);

            StudentServlet servlet = new StudentServlet();
            servlet.doPost(request, response);
            writer.flush();

            studentToAdd.setId(new StudentRepository().lastId()); //since added student is the new object

            ResponseResult<Student> expectedResp = new ResponseResult<>(studentToAdd);

            assertEquals(expectedResp.jsonToString(), stringWriter.toString());
        }

        private static Student studentToAdd = new Student(-1, "added", 20, new Random().nextLong(), 1000);

        @AfterAll
        static void deletingAddedStudent() throws IOException {
            new StudentRepository().delete(studentToAdd.getId());
        }
    }

    @Nested
    class doPutTests {
        private StringWriter stringWriter;

        @BeforeEach
        void initializeRequest() throws IOException {
            stringWriter = new StringWriter();
            PrintWriter writer = new PrintWriter(stringWriter);
            when(response.getWriter()).thenReturn(writer);
        }

        @Test
        void doPutNotFound() throws IOException, ServletException {
            when(request.getParameter("id")).thenReturn("-1");

            StudentServlet servlet = new StudentServlet();
            servlet.doPut(request, response);
            response.getWriter().flush();

            assertEquals("{\"message\":\"Cannot find object with this ID\"}", stringWriter.toString().trim());
        }

        @Test
        void doPutNotInteger() throws IOException, ServletException {
            when(request.getParameter("id")).thenReturn("1");
            when(request.getParameter("name")).thenReturn("throw");
            when(request.getParameter("age")).thenReturn("non");

            StudentServlet servlet = new StudentServlet();
            servlet.doPut(request, response);
            response.getWriter().flush();

            assertEquals("{\"message\":\"java.lang.NumberFormatException: For input string: \\\"non\\\"\"}", stringWriter.toString().trim());
        }

        @ParameterizedTest
        @MethodSource("studentProvider")
        void doPutMainScenario(Student studentToAdd) throws IOException, ServletException {
            Repository<Student> realRepo = new StudentRepository();
            Student studentToReplace = new Student();
            studentToReplace.setNum(new Random().nextLong()); // in very rare cases this test might not run due to this student already is presented in real repo

            Assumptions.assumeTrue(realRepo.add(studentToReplace));
            Assumptions.assumeFalse(new StudentRepository().takeData().contains(studentToAdd));

            idDelete = studentToReplace.getId();

            when(request.getParameter("id")).thenReturn(String.valueOf(studentToReplace.getId())); //id replacement
            when(request.getParameter("name")).thenReturn(studentToAdd.getName());
            when(request.getParameter("age")).thenReturn(String.valueOf(studentToAdd.getAge()));
            when(request.getParameter("num")).thenReturn(String.valueOf(studentToAdd.getNum()));
            when(request.getParameter("salary")).thenReturn(String.valueOf(studentToAdd.getSalary()));

            studentToAdd.setId(studentToReplace.getId()); //since added student is the new object

            StudentServlet servlet = new StudentServlet();
            servlet.doPut(request, response);
            response.getWriter().flush();

            ResponseResult<Student> expectedResp = new ResponseResult<>(studentToReplace);


            assertEquals(expectedResp.jsonToString(), stringWriter.toString());
            assertEquals(studentToAdd, new StudentRepository().takeData(studentToReplace.getId()));
        }

        private static List<Student> studentProvider() {
            //to all students id will be added
            Random rand = new Random();
            Student emptyStudent = new Student();

            Student onlyAge = new Student();
            onlyAge.setAge(5);

            Student onlyName = new Student();
            onlyName.setName("OnlyName");
            Student nameAge = new Student();
            nameAge.setName("NameAge");
            nameAge.setAge(10);
            Student nameAgeNum = new Student();
            nameAgeNum.setName("nameAgeNum");
            nameAgeNum.setAge(15);
            nameAgeNum.setNum(rand.nextLong());

            Student full = new Student(-1, "Full", 20, rand.nextLong(), rand.nextInt());

            return List.of(emptyStudent, onlyAge, onlyName, nameAgeNum, full);
        }

        private int idDelete;

        @AfterEach
        public void deletingAddedStudent() throws IOException {
            new StudentRepository().delete(idDelete);
        }
    }

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

            assertEquals("{\"message\":\"Cannot find object with this ID\"}", stringWriter.toString().trim());
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
            Assumptions.assumeTrue(repo.size() > 0);

            Student existingStudent = repo.takeData().stream().findAny().get();

            when(request.getParameter("id")).thenReturn(String.valueOf(existingStudent.getId()));

            StringWriter stringWriter = new StringWriter();
            PrintWriter writer = new PrintWriter(stringWriter);
            when(response.getWriter()).thenReturn(writer);

            StudentServlet servlet = new StudentServlet();
            servlet.doGet(request, response);
            writer.flush();

            ResponseResult<Student> expectedResp = new ResponseResult<>(existingStudent);

            assertEquals(expectedResp.jsonToString(), stringWriter.toString());
        }

        @Test
        void doGetAll() throws IOException {

            when(request.getParameter("id")).thenReturn(null);

            StringWriter stringWriter = new StringWriter();
            PrintWriter writer = new PrintWriter(stringWriter);
            when(response.getWriter()).thenReturn(writer);

            StudentServlet servlet = new StudentServlet();
            servlet.doGet(request, response);
            writer.flush();

            ResponseResult<Collection<Student>> expectedResp = new ResponseResult<>(new StudentRepository().takeData());

            assertEquals(expectedResp.jsonToString(), stringWriter.toString());
        }
    }

    @Nested
    class doDeleteTest {
        @Test
        void doGetNotFound() throws IOException, ServletException {
            when(request.getParameter("id")).thenReturn("-1");
            StringWriter stringWriter = new StringWriter();
            PrintWriter writer = new PrintWriter(stringWriter);
            when(response.getWriter()).thenReturn(writer);

            StudentServlet servlet = new StudentServlet();
            servlet.doDelete(request, response);
            writer.flush();

            assertEquals("{\"message\":\"Cannot find object with this ID\"}", stringWriter.toString().trim());

        }

        @Test
        void noIdProvided() throws IOException, ServletException {
            when(request.getParameter("id")).thenReturn(null);
            StringWriter stringWriter = new StringWriter();
            PrintWriter writer = new PrintWriter(stringWriter);
            when(response.getWriter()).thenReturn(writer);

            StudentServlet servlet = new StudentServlet();
            servlet.doDelete(request, response);
            writer.flush();

            assertEquals("{\"message\":\"Please provide object ID\"}", stringWriter.toString().trim());

        }

        @Test
        void doDeleteNotInteger() throws IOException, ServletException {
            when(request.getParameter("id")).thenReturn("1l");
            StringWriter stringWriter = new StringWriter();
            PrintWriter writer = new PrintWriter(stringWriter);
            when(response.getWriter()).thenReturn(writer);

            StudentServlet servlet = new StudentServlet();
            servlet.doDelete(request, response);
            writer.flush();

            assertEquals("{\"message\":\"java.lang.NumberFormatException: For input string: \\\"1l\\\" Id must be an integer\"}", stringWriter.toString().trim());
        }

        @Test
        void doDeleteMainScenario() throws IOException, ServletException {
            Repository<Student> realRepo = new StudentRepository();
            Student studentToTempAdd = new Student();
            studentToTempAdd.setNum(new Random().nextLong()); // in very rare cases this test might fail due to this student already is presented in real repo
            Assumptions.assumeTrue(realRepo.add(studentToTempAdd));

            when(request.getParameter("id")).thenReturn(String.valueOf(studentToTempAdd.getId()));
            StringWriter stringWriter = new StringWriter();
            PrintWriter writer = new PrintWriter(stringWriter);
            when(response.getWriter()).thenReturn(writer);

            StudentServlet servlet = new StudentServlet();
            servlet.doDelete(request, response);
            writer.flush();

            ResponseResult<Student> expectedResp = new ResponseResult<>(studentToTempAdd);

            assertEquals(expectedResp.jsonToString(), stringWriter.toString());
        }
    }

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

}