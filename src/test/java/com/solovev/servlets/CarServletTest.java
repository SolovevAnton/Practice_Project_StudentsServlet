package com.solovev.servlets;

import com.solovev.dto.ResponseResult;
import com.solovev.model.Car;
import com.solovev.model.Student;
import com.solovev.repositories.CarRepository;
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

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Year;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.function.BiConsumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CarServletTest {
    /**
     * Tests for throwing different messages for errors
     */
    @Nested
    class errorsTest {
        @ParameterizedTest(name = "{index} method in list")
        @MethodSource("idMethodsProvider")
        public void exceptionInId(BiConsumer<HttpServletRequest, HttpServletResponse> method) throws IOException {
            when(request.getParameter("id")).thenReturn("1l");

            method.accept(request, response);
            response.getWriter().flush();

            assertEquals("{\"message\":\"java.lang.NumberFormatException: For input string: \\\"1l\\\"\"}", stringWriter.toString().trim());
        }

        @ParameterizedTest(name = "{index} method in list")
        @MethodSource("idMethodsProvider")
        public void noIdFound(BiConsumer<HttpServletRequest, HttpServletResponse> method) throws IOException {
            when(request.getParameter("id")).thenReturn("-1");

            method.accept(request, response);
            response.getWriter().flush();

            assertEquals("{\"message\":\"Cannot find object with this ID\"}", stringWriter.toString().trim());
        }

        @ParameterizedTest(name = "{index} method in list")
        @MethodSource("creatingCarMethodsProvider")
        public void exceptionInYear(BiConsumer<HttpServletRequest, HttpServletResponse> method) throws IOException {
            Assumptions.assumeTrue(repo.size() > 0);
            when(request.getParameter("id")).thenReturn("1");
            when(request.getParameter("brand")).thenReturn("yearFail");
            when(request.getParameter("power")).thenReturn("0");
            when(request.getParameter("year")).thenReturn("17784");

            method.accept(request, response);
            response.getWriter().flush();

            assertEquals("{\"message\":\"java.time.format.DateTimeParseException: Text '17784' could not be parsed at index 0\"}", stringWriter.toString().trim());
        }

        @ParameterizedTest(name = "{index} method in list")
        @MethodSource("creatingCarMethodsProvider")
        public void exceptionInPower(BiConsumer<HttpServletRequest, HttpServletResponse> method) throws IOException {
            Assumptions.assumeTrue(repo.size() > 0);
            when(request.getParameter("id")).thenReturn("1");
            when(request.getParameter("brand")).thenReturn(null);
            when(request.getParameter("power")).thenReturn("2.5");


            method.accept(request, response);
            response.getWriter().flush();

            assertEquals("{\"message\":\"java.lang.NumberFormatException: For input string: \\\"2.5\\\"\"}", stringWriter.toString().trim());
        }

        @Test
        public void exceptionPostAlreadyExists() throws ServletException, IOException {
            Assumptions.assumeTrue(repo.size() > 0);

            Car existingCar = repo.takeData().stream().findAny().get();

            requestCreator(existingCar);

            servlet.doPost(request, response);
            response.getWriter().flush();

            assertEquals("{\"message\":\"This object already exists in database\"}", stringWriter.toString().trim());
        }

        public static List<BiConsumer<HttpServletRequest, HttpServletResponse>> idMethodsProvider() {
            return List.of(doGet, doDelete, doPut);
        }

        public static List<BiConsumer<HttpServletRequest, HttpServletResponse>> creatingCarMethodsProvider() {
            return List.of(doPost, doPut);
        }

        private static CarServlet servlet = new CarServlet();
        private static BiConsumer<HttpServletRequest, HttpServletResponse> doGet = (req, resp) -> {
            try {
                servlet.doGet(req, resp);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
        private static BiConsumer<HttpServletRequest, HttpServletResponse> doPost = (req, resp) -> {
            try {
                servlet.doPost(req, resp);
            } catch (IOException | ServletException e) {
                throw new RuntimeException(e);
            }
        };
        private static BiConsumer<HttpServletRequest, HttpServletResponse> doDelete = (req, resp) -> {
            try {
                servlet.doDelete(req, resp);
            } catch (IOException | ServletException e) {
                throw new RuntimeException(e);
            }
        };
        private static BiConsumer<HttpServletRequest, HttpServletResponse> doPut = (req, resp) -> {
            try {
                servlet.doPut(req, resp);
            } catch (IOException | ServletException e) {
                throw new RuntimeException(e);
            }
        };
    }


    @ParameterizedTest
    @MethodSource("existingCarProvider")
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

    @ParameterizedTest
    @MethodSource("someCarProvider")
    public void doPost(Car carToAdd) throws ServletException, IOException {
        Assumptions.assumeFalse(repo.takeData().contains(carToAdd));
        idToDeleteFromRepo = repo.lastId() + 1; // +1 since value can be added

        requestCreator(carToAdd);

        servlet.doPost(request, response);
        response.getWriter().flush();

        carToAdd.setId(idToDeleteFromRepo); //since added student is the new object

        ResponseResult<Car> expectedResp = new ResponseResult<>(carToAdd);

        assertEquals(expectedResp.jsonToString(), stringWriter.toString());
    }

    @ParameterizedTest
    @MethodSource("someCarProvider")
    public void doDelete(Car carToDelete) throws ServletException, IOException {
        Assumptions.assumeTrue(repo.add(carToDelete));
        idToDeleteFromRepo = carToDelete.getId(); //to be deleted even if test fails

        when(request.getParameter("id")).thenReturn(String.valueOf(idToDeleteFromRepo));

        servlet.doDelete(request, response);
        response.getWriter().flush();

        ResponseResult<Car> expectedResp = new ResponseResult<>(carToDelete);

        assertEquals(expectedResp.jsonToString(), stringWriter.toString());
        assertFalse(new CarRepository().takeData().contains(carToDelete));
    }

    @ParameterizedTest
    @MethodSource("someCarProvider")
    public void doReplace(Car carReplacement) throws ServletException, IOException {
        Car carToBeReplaced = new Car();
        carToBeReplaced.setBrand("toBeReplaced");
        Assumptions.assumeTrue(repo.add(carToBeReplaced)); //test fails if this car alreadyExists
        idToDeleteFromRepo = carToBeReplaced.getId(); //to be deleted even if test fails
        assertFalse(new CarRepository().takeData().contains(carReplacement));//Test will Fail if car already exits, so the AfterEach cleaner will be executed!

        when(request.getParameter("id")).thenReturn(String.valueOf(idToDeleteFromRepo));
        requestCreator(carReplacement);

        servlet.doPut(request, response);
        response.getWriter().flush();

        ResponseResult<Car> expectedResp = new ResponseResult<>(carToBeReplaced);

        assertEquals(expectedResp.jsonToString(), stringWriter.toString());
        assertEquals(carReplacement,new CarRepository().takeData(idToDeleteFromRepo));
    }

    private StringWriter stringWriter;
    private Repository<Car> repo;
    private final CarServlet servlet = new CarServlet();
    private int idToDeleteFromRepo;

    @BeforeEach
    public void initializeRequest() throws IOException {
        stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);

        repo = new CarRepository();
    }

    @AfterEach
    public void repoCleaner() throws IOException {
        if (idToDeleteFromRepo != 0) {
            new CarRepository().delete(idToDeleteFromRepo);
            idToDeleteFromRepo = 0;
        }
    }
    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;
    private void requestCreator(Car existingCar) {
        when(request.getParameter("brand")).thenReturn(existingCar.getBrand());
        when(request.getParameter("power")).thenReturn(String.valueOf(existingCar.getPower()));
        when(request.getParameter("year"))
                .thenReturn(existingCar.getYear() != null ? existingCar.getYear().toString() : null);
        when(request.getParameter("idStudent")).thenReturn(String.valueOf(existingCar.getIdStudent()));
    }
    public static Collection<Car> existingCarProvider() throws IOException {
        Repository<Car> cars = new CarRepository();
        return cars.size() > 0 ? cars.takeData() : List.of(new Car());
    }

    public static List<Car> someCarProvider() {
        //to all car id will be added
        Random rand = new Random();
        Car emptyCar = new Car();

        Car onlyBrand = new Car();
        onlyBrand.setBrand("Only Brand");

        Car onlyPower = new Car();
        onlyPower.setPower(rand.nextInt());

        Car brandPower = new Car();
        brandPower.setBrand("BrandPower");
        brandPower.setPower(10);

        Car brandPowerYear = new Car();
        brandPowerYear.setBrand("brandPowerYear");
        brandPowerYear.setPower(15);
        brandPowerYear.setYear(Year.parse("1980"));

        Car full = new Car(-1, "Full", 20, Year.parse("1950"), Integer.MAX_VALUE);

        return List.of(emptyCar, onlyBrand, onlyPower, brandPower, brandPowerYear, full);
    }

}