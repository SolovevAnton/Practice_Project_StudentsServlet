package com.solovev;

import com.solovev.model.Car;
import com.solovev.repositories.CarRepository;
import com.solovev.repositories.Repository;

import java.io.IOException;
import java.time.Year;
import java.util.Set;

public class Main {
    public static void main(String[] args) throws IOException {
        Set<Car> cars = Set.of(
                new Car(0, "AUDI", 125, Year.parse("2012"), 1),
                new Car(0, "BMW", 225, Year.parse("2009"), 2),
                new Car(0, "Reno", 105, Year.parse("2014"), 3)
        );

        Repository<Car> reloadRepo = new CarRepository();
        reloadRepo.clear();
        cars.forEach(reloadRepo:: add);
    }
}