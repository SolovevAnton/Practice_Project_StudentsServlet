package com.solovev.repositories;

import com.solovev.util.Constants;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Collection;

public class CarRepository<T> implements Repository<T> {
    private Connection connection;

    public CarRepository() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            this.connection = DriverManager.getConnection(Constants.DB_URL, Constants.USERNAME, Constants.PASSWORD);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    public boolean add(T elem) {
        return false;
    }

    @Override
    public T delete(int elemId) {
        return null;
    }

    @Override
    public Collection<T> takeData() {
        return null;
    }

    @Override
    public T takeData(int elemId) {
        return null;
    }

    @Override
    public boolean replace(T newElem) {
        return false;
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public int lastId() {
        return 0;
    }

    @Override
    public void close() throws Exception {

    }
}
