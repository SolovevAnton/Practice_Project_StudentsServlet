package com.solovev.servlets;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class StudentServletTest {
    @Nested
    class doPostTests {
        @Test
        void doPostExists() throws IOException{
            StudentRepository repo = new StudentRepository();
            Assumptions.assumeTrue(repo.size() > 0);

            Student existingStudent = repo.takeData().stream().findAny().get();

            when(request.getParameter("name")).thenReturn(existingStudent.getName());
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
        void doPostExistsJson() throws IOException{
            StudentRepository repo = new StudentRepository();
            Assumptions.assumeTrue(repo.size() > 0);

            Student existingStudent = repo.takeData().stream().findAny().get();

            ObjectMapper objectMapper = new ObjectMapper();
            // Set the Content-Type header to application/json
            when(request.getHeader("Content-Type")).thenReturn("application/json");

            // Create a BufferedReader with the JSON content
            BufferedReader reader = new BufferedReader(new StringReader(objectMapper.writeValueAsString(existingStudent)));

            // Set up the getReader() method of the HttpServletRequest to return the BufferedReader
            when(request.getReader()).thenReturn(reader);


            StringWriter stringWriter = new StringWriter();
            PrintWriter writer = new PrintWriter(stringWriter);
            when(response.getWriter()).thenReturn(writer);

            StudentServlet servlet = new StudentServlet();
            servlet.doPost(request, response);
            writer.flush();

            assertEquals("{\"message\":\"This object already exists in database\"}", stringWriter.toString().trim());
        }

        @Test
        void doPostNotInteger() throws IOException {
            when(request.getParameter("name")).thenReturn(null);
            when(request.getParameter("age")).thenReturn(null);
            when(request.getParameter("num")).thenReturn("1l");

            StringWriter stringWriter = new StringWriter();
            PrintWriter writer = new PrintWriter(stringWriter);
            when(response.getWriter()).thenReturn(writer);

            StudentServlet servlet = new StudentServlet();
            servlet.doPost(request, response);
            writer.flush();

            assertEquals("{\"message\":\"Error: java.lang.NumberFormatException: For input string: \\\"1l\\\"\"}", stringWriter.toString().trim());
        }
        @Test
        void doPostCorruptedJson() throws IOException{
            // Set the Content-Type header to application/json
            when(request.getHeader("Content-Type")).thenReturn("application/json");
            String corruptedObject = "{\"id\":3,\"name\":null,\"age\":2h,\"num\":3,\"salary\":3}";

            // Create a BufferedReader with the JSON content
            BufferedReader reader = new BufferedReader(new StringReader(corruptedObject));

            // Set up the getReader() method of the HttpServletRequest to return the BufferedReader
            when(request.getReader()).thenReturn(reader);

            StringWriter stringWriter = new StringWriter();
            PrintWriter writer = new PrintWriter(stringWriter);
            when(response.getWriter()).thenReturn(writer);

            StudentServlet servlet = new StudentServlet();
            servlet.doPost(request, response);
            writer.flush();

            assertEquals("{\"message\":\"Error: com.fasterxml.jackson.core.JsonParseException: Unexpected character ('h' (code 104)): was expecting comma to separate Object entries\\n at [Source: (BufferedReader); line: 1, column: 29]\"}"
                    , stringWriter.toString().trim());
        }

        @Test
        void doPostMainScenario() throws IOException{
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
        @Test
        void doPostJsonObjectAdding() throws IOException{
            Assumptions.assumeFalse(new StudentRepository().takeData().contains(studentToAdd));
            ObjectMapper objectMapper = new ObjectMapper();
            // Set the Content-Type header to application/json
            when(request.getHeader("Content-Type")).thenReturn("application/json");

            // Create a BufferedReader with the JSON content
            BufferedReader reader = new BufferedReader(new StringReader(objectMapper.writeValueAsString(studentToAdd)));

            // Set up the getReader() method of the HttpServletRequest to return the BufferedReader
            when(request.getReader()).thenReturn(reader);

            StringWriter stringWriter = new StringWriter();
            PrintWriter writer = new PrintWriter(stringWriter);
            when(response.getWriter()).thenReturn(writer);

            StudentServlet servlet = new StudentServlet();
            servlet.doPost(request, response);
            writer.flush();

            studentToAdd.setId(new StudentRepository().lastId()); //since added student is the new object

            ResponseResult<Student> expectedResp = new ResponseResult<>(studentToAdd);

            assertEquals(expectedResp.jsonToString(), stringWriter.toString());
            assertEquals(studentToAdd, new StudentRepository().takeData(studentToAdd.getId()));
        }
        @Test
        public void doPostIgnoreFields() throws IOException {
            Assumptions.assumeFalse(new StudentRepository().takeData().contains(studentToAdd));
            ObjectMapper objectMapper = new ObjectMapper();
            Student studentToIgnore = new Student(-1,"toIgnore",5,10,10);
            // Set the Content-Type header to application/json
            when(request.getHeader("Content-Type")).thenReturn("application/json");
            when(request.getParameter("name")).thenReturn(studentToIgnore.getName());
            when(request.getParameter("age")).thenReturn(String.valueOf(studentToIgnore.getAge()));
            when(request.getParameter("num")).thenReturn(String.valueOf(studentToIgnore.getNum()));
            when(request.getParameter("salary")).thenReturn(String.valueOf(studentToIgnore.getSalary()));
            // Create a BufferedReader with the JSON content
            BufferedReader reader = new BufferedReader(new StringReader(objectMapper.writeValueAsString(studentToAdd)));

            // Set up the getReader() method of the HttpServletRequest to return the BufferedReader
            when(request.getReader()).thenReturn(reader);

            StringWriter stringWriter = new StringWriter();
            PrintWriter writer = new PrintWriter(stringWriter);
            when(response.getWriter()).thenReturn(writer);

            StudentServlet servlet = new StudentServlet();
            servlet.doPost(request, response);
            writer.flush();

            studentToAdd.setId(new StudentRepository().lastId()); //since added student is the new object

            ResponseResult<Student> expectedResp = new ResponseResult<>(studentToAdd);

            assertEquals(expectedResp.jsonToString(), stringWriter.toString());
            assertEquals(studentToAdd, new StudentRepository().takeData(studentToAdd.getId()));
            assertFalse(new StudentRepository().takeData().contains(studentToIgnore));
        }

        private final Student studentToAdd = new Student(-1, "added", 20, new Random().nextLong(), 1000);

        @AfterEach
        void deletingAddedStudent() throws IOException {
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
        void doPutNotFoundJson() throws IOException, ServletException {
            Student studentToAdd = new Student(-1, "added", 20, new Random().nextLong(), 1000);

            ObjectMapper objectMapper = new ObjectMapper();
            // Set the Content-Type header to application/json
            when(request.getHeader("Content-Type")).thenReturn("application/json");

            // Create a BufferedReader with the JSON content
            BufferedReader reader = new BufferedReader(new StringReader(objectMapper.writeValueAsString(studentToAdd)));

            // Set up the getReader() method of the HttpServletRequest to return the BufferedReader
            when(request.getReader()).thenReturn(reader);

            StudentServlet servlet = new StudentServlet();
            servlet.doPut(request, response);
            response.getWriter().flush();

            assertEquals("{\"message\":\"Cannot find object with this ID\"}", stringWriter.toString().trim());
        }
        @Test
        public void putNoId() throws ServletException, IOException {
            when(request.getParameter("id")).thenReturn(null);

            StudentServlet servlet = new StudentServlet();
            servlet.doPut(request, response);
            response.getWriter().flush();

            assertEquals("{\"message\":\"Please provide object ID\"}", stringWriter.toString().trim());
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
            Assumptions.assumeTrue(new StudentRepository().size()>0);

            when(request.getParameter("id")).thenReturn("1");
            when(request.getParameter("name")).thenReturn("throw");
            when(request.getParameter("age")).thenReturn("non");

            StudentServlet servlet = new StudentServlet();
            servlet.doPut(request, response);
            response.getWriter().flush();

            assertEquals("{\"message\":\"Error: java.lang.NumberFormatException: For input string: \\\"non\\\"\"}"
                    , stringWriter.toString().trim());
        }
        @Test
        void doPutJsonParserError() throws IOException, ServletException {
            Assumptions.assumeTrue(new StudentRepository().size()>0);
            // Set the Content-Type header to application/json
            when(request.getHeader("Content-Type")).thenReturn("application/json");
            String corruptedObject = "{\"id\":1,\"name\":null,\"age\":2h,\"num\":3,\"salary\":3}";

            // Create a BufferedReader with the JSON content
            BufferedReader reader = new BufferedReader(new StringReader(corruptedObject));

            // Set up the getReader() method of the HttpServletRequest to return the BufferedReader
            when(request.getReader()).thenReturn(reader);

            StudentServlet servlet = new StudentServlet();
            servlet.doPut(request, response);
            response.getWriter().flush();

            assertEquals("{\"message\":\"Error: com.fasterxml.jackson.core.JsonParseException: Unexpected character ('h' (code 104)): was expecting comma to separate Object entries\\n at [Source: (BufferedReader); line: 1, column: 29]\"}"
                    , stringWriter.toString().trim());
        }
        @Test
        public void doPutIgnoreParameters() throws IOException, ServletException {
            Student studentToIgnore = new Student(-1,"toIgnore",5,10,10);
            Student studentToAdd = new Student(-1, "added", 20, new Random().nextLong(), 1000);

            Repository<Student> realRepo = new StudentRepository();
            Student studentToReplace = new Student();
            studentToReplace.setNum(new Random().nextLong()); // in very rare cases this test might not run due to this student already is presented in real repo
            Assumptions.assumeTrue(realRepo.add(studentToReplace));
            idDelete = studentToReplace.getId();
            assertFalse(new StudentRepository().takeData().contains(studentToIgnore)); //Test will Fail if car already exits, so the AfterEach cleaner will be executed!
            assertFalse(new StudentRepository().takeData().contains(studentToAdd)); //Test will Fail if car already exits, so the AfterEach cleaner will be executed!
            studentToIgnore.setId(studentToReplace.getId());
            studentToAdd.setId(studentToReplace.getId());

            // Set the Content-Type header to application/json
            when(request.getHeader("Content-Type")).thenReturn("application/json");
            when(request.getParameter("id")).thenReturn(String.valueOf(studentToReplace.getId())); //id replacement
            when(request.getParameter("name")).thenReturn(studentToIgnore.getName());
            when(request.getParameter("age")).thenReturn(String.valueOf(studentToIgnore.getAge()));
            when(request.getParameter("num")).thenReturn(String.valueOf(studentToIgnore.getNum()));
            when(request.getParameter("salary")).thenReturn(String.valueOf(studentToIgnore.getSalary()));

            ObjectMapper objectMapper = new ObjectMapper();
            // Set the Content-Type header to application/json
            when(request.getHeader("Content-Type")).thenReturn("application/json");

            // Create a BufferedReader with the JSON content
            BufferedReader reader = new BufferedReader(new StringReader(objectMapper.writeValueAsString(studentToAdd)));

            // Set up the getReader() method of the HttpServletRequest to return the BufferedReader
            when(request.getReader()).thenReturn(reader);

            studentToAdd.setId(studentToReplace.getId()); //since added student is the new object

            StudentServlet servlet = new StudentServlet();
            servlet.doPut(request, response);
            response.getWriter().flush();

            ResponseResult<Student> expectedResp = new ResponseResult<>(studentToReplace);


            assertEquals(expectedResp.jsonToString(), stringWriter.toString());
            assertEquals(studentToAdd, new StudentRepository().takeData(studentToReplace.getId()));
            assertFalse(new StudentRepository().takeData().contains(studentToReplace));
            assertFalse(new StudentRepository().takeData().contains(studentToIgnore));
        }
        @ParameterizedTest
        @MethodSource("studentProvider")
        void doPutMainScenario(Student studentToAdd) throws IOException, ServletException {
            Repository<Student> realRepo = new StudentRepository();
            Student studentToReplace = new Student();
            studentToReplace.setNum(new Random().nextLong()); // in very rare cases this test might not run due to this student already is presented in real repo
            Assumptions.assumeTrue(realRepo.add(studentToReplace));
            idDelete = studentToReplace.getId();
            assertFalse(new StudentRepository().takeData().contains(studentToAdd)); //Test will Fail if car already exits, so the AfterEach cleaner will be executed!



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
            assertFalse(new StudentRepository().takeData().contains(studentToReplace));
        }
        @ParameterizedTest
        @MethodSource("studentProvider")
        void doPutMainJson(Student studentToAdd) throws IOException, ServletException {
            Repository<Student> realRepo = new StudentRepository();
            Student studentToReplace = new Student();
            studentToReplace.setNum(new Random().nextLong()); // in very rare cases this test might not run due to this student already is presented in real repo
            Assumptions.assumeTrue(realRepo.add(studentToReplace));
            idDelete = studentToReplace.getId();
            assertFalse(new StudentRepository().takeData().contains(studentToAdd)); //Test will Fail if car already exits, so the AfterEach cleaner will be executed!

            studentToAdd.setId(idDelete); //since added student is the new object

            ObjectMapper objectMapper = new ObjectMapper();
            // Set the Content-Type header to application/json
            when(request.getHeader("Content-Type")).thenReturn("application/json");

            // Create a BufferedReader with the JSON content
            BufferedReader reader = new BufferedReader(new StringReader(objectMapper.writeValueAsString(studentToAdd)));

            // Set up the getReader() method of the HttpServletRequest to return the BufferedReader
            when(request.getReader()).thenReturn(reader);



            StudentServlet servlet = new StudentServlet();
            servlet.doPut(request, response);
            response.getWriter().flush();

            ResponseResult<Student> expectedResp = new ResponseResult<>(studentToReplace);


            assertEquals(expectedResp.jsonToString(), stringWriter.toString());
            assertEquals(studentToAdd, new StudentRepository().takeData(studentToReplace.getId()));
            assertFalse(new StudentRepository().takeData().contains(studentToReplace));
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

            return List.of(emptyStudent, onlyAge,nameAge, onlyName, nameAgeNum, full);
        }

        private int idDelete;

        @AfterEach
        public void repoCleaner() throws IOException {
            if (idDelete != 0) {
                new StudentRepository().delete(idDelete);
                idDelete = 0;
            }
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
        public void deleteNoId() throws ServletException, IOException {
            when(request.getParameter("id")).thenReturn(null);
            StringWriter stringWriter = new StringWriter();
            PrintWriter writer = new PrintWriter(stringWriter);
            when(response.getWriter()).thenReturn(writer);

            StudentServlet servlet = new StudentServlet();
            servlet.doDelete(request, response);
            response.getWriter().flush();

            assertEquals("{\"message\":\"Please provide object ID\"}", stringWriter.toString().trim());
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