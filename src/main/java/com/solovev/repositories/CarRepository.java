package com.solovev.repositories;

import com.solovev.model.Car;
import com.solovev.util.Constants;

import java.sql.*;
import java.time.Year;
import java.util.ArrayList;
import java.util.Collection;

public class CarRepository implements Repository<Car> {
    private Connection connection;

    public CarRepository() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            this.connection = DriverManager.getConnection(Constants.DB_URL, Constants.USERNAME, Constants.PASSWORD);
        } catch (Exception e) {
            e.printStackTrace();
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

    /**
     * prepares statement based on the car value
     * statement passed as argument WILL BE MODIFIED
     *
     * @param statement to modify. Order must maintained: brand, power, year, student id
     * @param car       car to use for statement preparing
     * @return statement with modification
     */
    private PreparedStatement prepareStatement(PreparedStatement statement, Car car) throws SQLException {
        statement.setString(1, car.getBrand());
        statement.setInt(2, car.getPower());
        statement.setInt(3, car.getYear().getValue());
        statement.setInt(4, car.getIdStudent());
        return statement;
    }

    /**
     * Method helps to execute query with ONE result of single int and return it
     *
     * @param query to execute
     * @return int that got from DB or 0 if nothing
     */
    private int gettingOneLineIntFromDB(String query) {
        int resultInt = 0;
        try (PreparedStatement preparedStatement = this.connection.prepareStatement(query)) {
            ResultSet result = preparedStatement.executeQuery();
            result.next();
            resultInt = result.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return resultInt;
    }

    @Override
    public boolean add(Car carToAdd) {
        String queryToAdd = "INSERT INTO auto(brand,power,year,id_s) values(?,?,?,?)";
        int updateExecuted = -1; // to show that update failed, if it has failed

        try (PreparedStatement statement = connection.prepareStatement(queryToAdd)) {
            prepareStatement(statement, carToAdd);
            updateExecuted = statement.executeUpdate();
            carToAdd.setId(lastId()); //update car id
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return updateExecuted > 0;
    }

    @Override
    public Car delete(int elemId) {
        Car deletedCar = takeData(elemId);
        String queryToDelete = "DELETE FROM auto WHERE auto.id=?";
        try (PreparedStatement statement = connection.prepareStatement(queryToDelete)) {
            statement.setInt(1, elemId);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return deletedCar;
    }

    @Override
    public Collection<Car> takeData() {
        String query = "SELECT * FROM auto";
        Collection<Car> cars = new ArrayList<>();
        try (Statement statement = connection.createStatement()) {
            ResultSet carsSet = statement.executeQuery(query);
            while (carsSet.next()) {
                cars.add(carFactory(carsSet));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return cars;
    }

    /**
     * gets car with matching id
     *
     * @param elemId id of the element to take
     * @return If there are several cars last one with matching id if none null
     */
    @Override
    public Car takeData(int elemId) {
        String query = "SELECT * FROM auto WHERE id=?";
        Car car = null;
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, elemId);
            ResultSet setCar = statement.executeQuery();
            while (setCar.next()) {
                car = carFactory(setCar);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return car;
    }

    @Override
    public boolean replace(Car carToReplaceWith) {
        String sql = "UPDATE auto SET auto.brand=?, auto.power=?, auto.year=?, auto.id_s=? where auto.id=?";
        int updateExecuted = -1; // to show that update failed, if it has failed
        try (PreparedStatement preparedStatement = this.connection.prepareStatement(sql)) {
            prepareStatement(preparedStatement, carToReplaceWith);
            preparedStatement.setInt(5, carToReplaceWith.getId());
            updateExecuted = preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return updateExecuted > 0;
    }

    @Override
    public int size() {
        return gettingOneLineIntFromDB("SELECT COUNT(id) FROM auto");
    }

    @Override
    public int lastId() {
        return gettingOneLineIntFromDB("SELECT MAX(id) FROM auto");
    }

    @Override
    public void close() {
        if (connection != null) {
            try {
                this.connection.close();
            } catch (SQLException ignored) {
            }
        }
    }
}