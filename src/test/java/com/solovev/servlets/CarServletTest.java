package com.solovev.servlets;

import com.solovev.dto.ResponseResult;
import com.solovev.model.Car;
import com.solovev.model.Student;
import com.solovev.repositories.CarRepository;
import com.solovev.repositories.Repository;
import com.solovev.repositories.StudentRepository;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CarServletTest {
    /**
     * Tests for throwing different messages for errors
     */
    @Nested
    class errorsTest{
        @ParameterizedTest
        @MethodSource("methodsProvider")
        public void exceptionInId(BiConsumer<HttpServletRequest,HttpServletResponse> method) throws IOException {
            when(request.getParameter("id")).thenReturn("1l");

            method.accept(request, response);
            response.getWriter().flush();

            assertEquals("{\"message\":\"java.lang.NumberFormatException: For input string: \\\"1l\\\"\"}", stringWriter.toString().trim());
        }

        @ParameterizedTest
        @MethodSource("methodsProvider")
        public void noIdFound(BiConsumer<HttpServletRequest,HttpServletResponse> method) throws IOException{
            when(request.getParameter("id")).thenReturn("-1");

            method.accept(request, response);
            response.getWriter().flush();

            assertEquals("{\"message\":\"Cannot find object with this ID\"}", stringWriter.toString().trim());
        }
        public static List<BiConsumer<HttpServletRequest,HttpServletResponse>> methodsProvider() {
            CarServlet servlet = new CarServlet();
            BiConsumer<HttpServletRequest,HttpServletResponse> doGet = (req, resp) -> {
                try {
                    servlet.doGet(req, resp);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            };

            return List.of(doGet);
        }
    }
    @ParameterizedTest
    @MethodSource("carProvider")
    void doGet(Car existingCar) throws IOException {
         Assumptions.assumeTrue(repo.size() > 0);
            when(request.getParameter("id")).thenReturn(String.valueOf(existingCar.getId()));

            servlet.doGet(request, response);
            response.getWriter().flush();

            ResponseResult<Car> expectedResp = new ResponseResult<>(existingCar);

            assertEquals(expectedResp.jsonToString(), stringWriter.toString());
    }
    @Test
    void doGetAll() throws IOException {
        when(request.getParameter("id")).thenReturn(null);

        servlet.doGet(request, response);
        response.getWriter().flush();

        ResponseResult<Collection<Car>> expectedResp = new ResponseResult<>(repo.takeData());

        assertEquals(expectedResp.jsonToString(), stringWriter.toString());
    }
    private StringWriter stringWriter;
    private Repository<Car> repo;
    private final CarServlet servlet = new CarServlet();

    @BeforeEach
    void initializeRequest() throws IOException {
        stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);

        repo = new CarRepository();
    }
    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;
    public static Collection<Car> carProvider() throws IOException {
        Repository<Car> cars = new CarRepository();
        return cars.size() > 0 ? cars.takeData() : List.of(new Car());
    }

}