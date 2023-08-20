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
     * Method helps to execute cuery with ONE result of single int and return it
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
    public boolean add(Car elem) {
        return false;
    }

    @Override
    public Car delete(int elemId) {
        return null;
    }

    @Override
    public Collection<Car> takeData() {
        String query = "SELECT * FROM auto";
        Collection<Car> cars = new ArrayList<>();
        try (Statement statement = connection.createStatement()) {
            ResultSet carsSet = statement.executeQuery(query);
            while(carsSet.next()){
               cars.add(carFactory(carsSet));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return cars;
    }

    /**
     * gets car with matching id
     * @param elemId id of the element to take
     * @return If there are several cars last one with matching id if none null
     */
    @Override
    public Car takeData(int elemId) {
        String query = "SELECT * FROM auto WHERE id=?";
        Car car = null;
        try(PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1,elemId);
            ResultSet setCar = statement.executeQuery();
            while(setCar.next()){
                car = carFactory(setCar);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return car;
    }

    @Override
    public boolean replace(Car newElem) {
        return false;
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