package com.solovev.servlets;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.solovev.dto.ResponseResult;
import com.solovev.model.Car;
import com.solovev.repositories.CarRepository;
import com.solovev.repositories.Repository;
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
    class GroupMethodsTests {
        @ParameterizedTest(name = "{index} method in list")
        @MethodSource("idMethodsProvider")
        public void exceptionInId(BiConsumer<HttpServletRequest, HttpServletResponse> method) throws IOException {
            when(request.getParameter("id")).thenReturn("1l");

            method.accept(request, response);
            response.getWriter().flush();

            assertEquals("{\"message\":\"Error: java.lang.NumberFormatException: For input string: \\\"1l\\\"\"}", stringWriter.toString().trim());
        }

        @ParameterizedTest(name = "{index} method in list")
        @MethodSource("idMethodsProvider")
        public void noIdFound(BiConsumer<HttpServletRequest, HttpServletResponse> method) throws IOException {
            when(request.getParameter("id")).thenReturn("0");

            method.accept(request, response);
            response.getWriter().flush();

            assertEquals("{\"message\":\"Cannot find object with this ID: " + "0" + "\"}", stringWriter.toString().trim());
        }

        @ParameterizedTest(name = "{index} method in list")
        @MethodSource("creatingCarMethodsProvider")
        public void jsonParsingException(BiConsumer<HttpServletRequest, HttpServletResponse> method) throws IOException {
            String corruptedObject = """
                      {
                        "id": 1,
                        "brand": "Reno",
                        "power": 3,
                        "year": 20023h,
                        "idStudent": 3
                      }\
                    """;
            requestCreator(corruptedObject);

            method.accept(request, response);
            response.getWriter().flush();
            idToDeleteFromRepo = repo.lastId() + 1; //to delete in case it was added

            assertEquals("{\"message\":\"Error: com.fasterxml.jackson.core.JsonParseException: Unexpected character ('h' (code 104)): was expecting comma to separate Object entries\\n at [Source: (BufferedReader); line: 5, column: 19]\"}"
                    , stringWriter.toString().trim());
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

            assertEquals("{\"message\":\"Error: java.time.format.DateTimeParseException: Text '17784' could not be parsed at index 0\"}", stringWriter.toString().trim());
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

            assertEquals("{\"message\":\"Error: java.lang.NumberFormatException: For input string: \\\"2.5\\\"\"}", stringWriter.toString().trim());
        }

        public static List<BiConsumer<HttpServletRequest, HttpServletResponse>> idMethodsProvider() {
            return List.of(doGet, doDelete, doPut);
        }

        public static List<BiConsumer<HttpServletRequest, HttpServletResponse>> creatingCarMethodsProvider() {
            return List.of(doPost, doPut);
        }

        private static final CarServlet servlet = new CarServlet();
        private static final BiConsumer<HttpServletRequest, HttpServletResponse> doGet = (req, resp) -> {
            try {
                servlet.doGet(req, resp);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
        private static final BiConsumer<HttpServletRequest, HttpServletResponse> doPost = (req, resp) -> {
            try {
                servlet.doPost(req, resp);
            } catch (IOException | ServletException e) {
                throw new RuntimeException(e);
            }
        };
        private static final BiConsumer<HttpServletRequest, HttpServletResponse> doDelete = (req, resp) -> {
            try {
                servlet.doDelete(req, resp);
            } catch (IOException | ServletException e) {
                throw new RuntimeException(e);
            }
        };
        private static final BiConsumer<HttpServletRequest, HttpServletResponse> doPut = (req, resp) -> {
            try {
                servlet.doPut(req, resp);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
    }

    @Nested
    class DoPostTests {
        @Test
        public void exceptionPostAlreadyExists() throws ServletException, IOException {
            Assumptions.assumeTrue(repo.size() > 0);

            Car existingCar = repo.takeData().stream().findAny().get();

            requestCreator(existingCar);

            servlet.doPost(request, response);
            response.getWriter().flush();

            assertEquals("{\"message\":\"This object already exists in database\"}", stringWriter.toString().trim());
        }

        @Test
        public void exceptionPostAlreadyExistsJson() throws ServletException, IOException {
            Assumptions.assumeTrue(repo.size() > 0);

            Car existingCar = repo.takeData().stream().findAny().get();

            requestCreator(objectMapper.writeValueAsString(existingCar));

            servlet.doPost(request, response);
            response.getWriter().flush();

            assertEquals("{\"message\":\"This object already exists in database\"}", stringWriter.toString().trim());
        }

        @ParameterizedTest
        @MethodSource("someCarProvider")
        public void doPost(Car carToAdd) throws ServletException, IOException {
            Assumptions.assumeFalse(repo.takeData().contains(carToAdd));
            idToDeleteFromRepo = repo.lastId() + 1; // +1 since value can be added

            requestCreator(carToAdd);

            servlet.doPost(request, response);
            response.getWriter().flush();

            carToAdd.setId(idToDeleteFromRepo); //since added is the new object

            ResponseResult<Car> expectedResp = new ResponseResult<>(carToAdd);

            assertEquals(expectedResp.jsonToString(), stringWriter.toString());
            assertTrue(new CarRepository().takeData().contains(carToAdd));
        }

        @ParameterizedTest
        @MethodSource("someCarProvider")
        public void doPostJson(Car carToAdd) throws ServletException, IOException {
            Assumptions.assumeFalse(repo.takeData().contains(carToAdd));
            idToDeleteFromRepo = repo.lastId() + 1; // +1 since value can be added

            requestCreator(objectMapper.writeValueAsString(carToAdd));

            servlet.doPost(request, response);
            response.getWriter().flush();

            carToAdd.setId(idToDeleteFromRepo); //since added is the new object

            ResponseResult<Car> expectedResp = new ResponseResult<>(carToAdd);

            assertEquals(expectedResp.jsonToString(), stringWriter.toString());
            assertTrue(new CarRepository().takeData().contains(carToAdd));
        }

        @Test
        public void jsonFieldsIgnorePost() throws IOException, ServletException {
            Car toAdd = new Car(1, "ToAdd", 2, Year.parse("1999"), 50);
            Car toIgnore = new Car(1, "ToIgnore", 3, Year.parse("1993"), 49);
            Assumptions.assumeFalse(repo.takeData().contains(toAdd));
            Assumptions.assumeFalse(repo.takeData().contains(toIgnore));

            idToDeleteFromRepo = repo.lastId() + 1; // +1 since value can be added

            requestCreator(toIgnore);
            requestCreator(objectMapper.writeValueAsString(toAdd));

            servlet.doPost(request, response);
            response.getWriter().flush();

            assertFalse(new CarRepository().takeData().contains(toIgnore));
            assertTrue(new CarRepository().takeData().contains(toAdd));
        }

        public static List<Car> someCarProvider() {
            return CarServletTest.someCarProvider();
        }
    }

    @Nested
    class DoDeleteTests {
        @Test
        public void provideIdMessageDelete() throws ServletException, IOException {
            when(request.getParameter("id")).thenReturn(null);
            servlet.doDelete(request, response);
            response.getWriter().flush();

            ResponseResult<Car> expectedResp = new ResponseResult<>("Please provide object ID");

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

        public static List<Car> someCarProvider() {
            return CarServletTest.someCarProvider();
        }
    }

    @Nested
    class DoGetTests {
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

        public static Collection<Car> existingCarProvider() throws IOException {
            return CarServletTest.existingCarProvider();
        }

    }

    @Nested
    class DoPutTests {
        @Test
        public void noIdFoundJsonPut() throws ServletException, IOException {
            Car carNoId = new Car(0, "NoID", 200, Year.of(1111), 999);
            ResponseResult<Car> expectedResp = new ResponseResult<>("Cannot find object with this ID: " + carNoId.getId());
            requestCreator(objectMapper.writeValueAsString(carNoId));

            servlet.doPut(request, response);
            response.getWriter().flush();

            assertEquals(expectedResp.jsonToString(), stringWriter.toString());
        }

        @Test
        public void provideIdMessagePut() throws ServletException, IOException {
            when(request.getParameter("id")).thenReturn(null);
            ResponseResult<Car> expectedResp = new ResponseResult<>("Please provide object ID");

            servlet.doPut(request, response);
            response.getWriter().flush();

            assertEquals(expectedResp.jsonToString(), stringWriter.toString());
        }

        @ParameterizedTest
        @MethodSource("someCarProvider")
        public void doPutJson(Car carReplacement) throws ServletException, IOException {
            Car carToBeReplaced = new Car();
            carToBeReplaced.setBrand("toBeReplaced");
            Assumptions.assumeTrue(repo.add(carToBeReplaced)); //test fails if this car alreadyExists
            idToDeleteFromRepo = carToBeReplaced.getId(); //to be deleted even if test fails
            assertFalse(new CarRepository().takeData().contains(carReplacement));//Test will Fail if car already exits, so the AfterEach cleaner will be executed!

            carReplacement.setId(idToDeleteFromRepo);

            requestCreator(objectMapper.writeValueAsString(carReplacement));

            servlet.doPut(request, response);
            response.getWriter().flush();

            ResponseResult<Car> expectedResp = new ResponseResult<>(carToBeReplaced);

            assertEquals(expectedResp.jsonToString(), stringWriter.toString());
            assertEquals(carReplacement, new CarRepository().takeData(idToDeleteFromRepo));
            assertFalse(new CarRepository().takeData().contains(carToBeReplaced));
        }

        @ParameterizedTest
        @MethodSource("someCarProvider")
        public void doPut(Car carReplacement) throws ServletException, IOException {
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
            assertEquals(carReplacement, new CarRepository().takeData(idToDeleteFromRepo));
            assertFalse(new CarRepository().takeData().contains(carToBeReplaced));
        }

        @Test
        public void jsonFieldsIgnorePut() throws IOException, ServletException {
            Car carToBeReplaced = new Car();
            carToBeReplaced.setBrand("toBeReplaced");
            Assumptions.assumeTrue(repo.add(carToBeReplaced)); //test fails if this car alreadyExists
            idToDeleteFromRepo = carToBeReplaced.getId(); //to be deleted even if test fails

            Car toAdd = new Car(idToDeleteFromRepo, "ToAdd", 2, Year.parse("1999"), 50);
            Car toIgnore = new Car(idToDeleteFromRepo, "ToIgnore", 3, Year.parse("1993"), 49);
            assertFalse(repo.takeData().contains(toAdd)); //Test will Fail if car already exits, so the AfterEach cleaner will be executed!
            assertFalse(repo.takeData().contains(toIgnore)); //Test will Fail if car already exits, so the AfterEach cleaner will be executed!

            requestCreator(toIgnore);
            requestCreator(objectMapper.writeValueAsString(toAdd));

            servlet.doPut(request, response);
            response.getWriter().flush();

            assertFalse(new CarRepository().takeData().contains(toIgnore));
            assertFalse(new CarRepository().takeData().contains(carToBeReplaced));
            assertTrue(new CarRepository().takeData().contains(toAdd));
        }

        public static List<Car> someCarProvider() {
            return CarServletTest.someCarProvider();
        }
    }

    private StringWriter stringWriter;
    private Repository<Car> repo;
    private final CarServlet servlet = new CarServlet();
    private int idToDeleteFromRepo;
    private final Collection<Car> initialRepo; //to check if something has happened to initial repo

    {
        try {
            initialRepo = new CarRepository().takeData();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

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
            if (!initialRepo.equals(new CarRepository().takeData())) {
                throw new RuntimeException("changed repo!");
            }
        }
    }

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    private void requestCreator(Car existingCar) {
        when(request.getParameter("brand")).thenReturn(existingCar.getBrand());
        when(request.getParameter("power")).thenReturn(String.valueOf(existingCar.getPower()));
        when(request.getParameter("year"))
                .thenReturn(existingCar.getYear() != null ? existingCar.getYear().toString() : null);
        when(request.getParameter("idStudent")).thenReturn(String.valueOf(existingCar.getIdStudent()));
    }

    /**
     * Creates request with json object presented in string
     *
     * @param existingCar object to include in request
     */
    private void requestCreator(String existingCar) throws IOException {
        when(request.getHeader("Content-Type")).thenReturn("application/json");
        BufferedReader reader = new BufferedReader(new StringReader(existingCar));
        when(request.getReader()).thenReturn(reader);
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