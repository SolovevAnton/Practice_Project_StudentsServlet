package com.solovev.repositories;

import com.solovev.model.Car;
import com.solovev.util.Constants;

import java.io.IOException;
import java.nio.file.Path;

public class CarRepository extends AbstractRepository<Car> {
    public CarRepository() throws IOException {
        this(Constants.CAR_FILE);
    }

    public CarRepository(Path path) throws IOException {
        super(path);
    }

    @Override
    public Class<Car> getType() {
        return Car.class;
    }
}
