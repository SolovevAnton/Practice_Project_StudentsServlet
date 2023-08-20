package com.solovev.repositories;

import com.solovev.model.Car;
import com.solovev.model.Student;
import com.solovev.util.Constants;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.sql.*;
import java.time.Year;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * All tests for now in DB used real DB!!
 */
class CarRepositoryTest {

    @Test
    void size() throws Exception {
        try (Repository<Car> repo = new CarRepository()) {
            assertEquals(carsInDb.size(), repo.size());
        }
    }

    @Test
    void lastId() throws Exception {
        try (Repository<Car> repo = new CarRepository()) {
            assertEquals(carsInDb
                            .stream()
                            .mapToInt(Car::getId)
                            .max()
                            .orElse(0),
                    repo.lastId());
        }
    }

    @Test
    void takeData() throws Exception {
        Class.forName("com.mysql.cj.jdbc.Driver");
        try (Repository<Car> repo = new CarRepository()) {
            assertEquals(carsInDb, repo.takeData());
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4})
    void testTakeData(int idToCheck) throws Exception {
        try (Repository<Car> repo = new CarRepository()) {
            assertEquals(carsInDb.toArray(new Car[0])[idToCheck - 1], repo.takeData(idToCheck)); //-1 since index and id difference
        }
    }
    @Test
    void takeDataNotFound(){
        try (CarRepository repo = new CarRepository()) {
            assertNull(repo.takeData(-1));
        }
    }

    @Test
    void addAndDeleteTestNormal() throws ClassNotFoundException, SQLException {
        String findVolvoQuery = "SELECT * FROM auto WHERE brand = 'Volvo'";
        Car carToAdd = new Car(-1,"Volvo", 200, Year.of(2009), 4);

        try (PreparedStatement preparedStatement =
                     connection
                             .prepareStatement(findVolvoQuery)) {
            //checks that base does not contain Volvo
            Assumptions.assumeFalse(preparedStatement.executeQuery().next());

            try (CarRepository repo = new CarRepository()) {
                //added test
                repo.add(carToAdd);
                ResultSet found = preparedStatement.executeQuery();
                found.next();
                Car carInDb = carFactory(found);

                assertEquals(carInDb, carToAdd);

                //delete test
                Car deletedCar = repo.delete(carInDb.getId());
                found = preparedStatement.executeQuery();

                assertEquals(carToAdd, deletedCar);
                assertFalse(found.next());
            }
        }
    }
    @Test
    void deleteNotFound(){
        try (CarRepository repo = new CarRepository()) {
            assertNull(repo.delete(-1));
        }
    }

    @BeforeEach
    @AfterEach
        /*
         * Method tests that database stays in its initial state after every test,
         * and fails if it's not, so it can be seen and recovered quickly
         */
    void dbTest() throws ClassNotFoundException, SQLException {
        Class.forName("com.mysql.cj.jdbc.Driver");
        String sql = "SELECT * FROM auto";
        Collection<Car> cars = new ArrayList<>();
        try (
                PreparedStatement preparedStatement =
                        DriverManager
                                .getConnection(Constants.DB_URL, Constants.USERNAME, Constants.PASSWORD)
                                .prepareStatement(sql)) {
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                cars.add(carFactory(resultSet));
            }
        }
        //checks if initial collection equals the real one if not throws
        if (!cars.equals(carsInDb)) {
            throw new RuntimeException(
                    String.format("Collections does not match\nexpected: %s \nfound:    %s",
                            carsInDb,
                            cars));
        }
    }

    /**
     * Creates car from one line of the result set
     *
     * @param set to create car with
     * @return created car
     */
    private Car carFactory(ResultSet set) throws SQLException {
        return new Car(
                set.getInt(1),
                set.getString(2),
                set.getInt(3),
                Year.of(set.getInt(4)),
                set.getInt(5)
        );
    }

    private final Collection<Car> carsInDb = List.of(
            new Car(1, "Vaz", 140, Year.of(1988), 1),
            new Car(2, "Vaz", 120, Year.of(1992), 1),
            new Car(3, "Vaz", 130, Year.of(1990), 2),
            new Car(4, "audi", 150, Year.of(1995), 2),
            new Car(5, "kia", 145, Year.of(2004), 3)

    );

    private Connection connection;

    {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(Constants.DB_URL, Constants.USERNAME, Constants.PASSWORD);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}