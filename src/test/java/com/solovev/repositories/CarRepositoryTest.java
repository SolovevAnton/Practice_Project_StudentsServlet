package com.solovev.repositories;

import com.solovev.model.Car;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Year;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CarRepositoryTest {
    @ParameterizedTest
    @NullSource
    void nullTests(Car car) {
        assertThrows(NullPointerException.class, () -> carRepo.add(car));
        assertThrows(NullPointerException.class, () -> carRepo.replace(car));
    }

    @Test
    void add() {
        assertTrue(carRepo.add(emptyCar));
        assertEquals(1, emptyCar.getId());

        assertTrue(carRepo.add(firstCar));
        assertFalse(carRepo.add(firstCar));

        assertEquals(2, firstCar.getId());

        assertTrue(carRepo.add(thirdCar));
        assertEquals(3, thirdCar.getId());
    }

    @Test
    void delete() {
        carRepo.add(emptyCar);
        carRepo.add(firstCar);

        assertEquals(emptyCar,carRepo.delete(1));
        assertNull(carRepo.delete(emptyCar.getId()));
        assertTrue(carRepo.add(emptyCar));
        assertEquals(3, emptyCar.getId());
    }

    @Test
    void takeData() {
        assertEquals(new HashSet<>(), carRepo.takeData());

        fillRepo();
        assertEquals(Set.of(emptyCar, firstCar, secondCar, thirdCar), carRepo.takeData());
    }

    @Test
    void intTakeData() {
        assertNull(carRepo.takeData(0));

        fillRepo();
        assertEquals(emptyCar, carRepo.takeData(1));
        assertEquals(thirdCar, carRepo.takeData(4));

        assertNull(carRepo.takeData(-1));
        assertNull(carRepo.takeData(5));
    }

    @Test
    void replace() {
        assertFalse(carRepo.replace(emptyCar));

        fillRepo();

        Car emptyReplacement = new Car();
        emptyReplacement.setId(1); //otherwise will not be replaced

        assertTrue(carRepo.replace(emptyReplacement));
        assertTrue(carRepo.replace(emptyReplacement));

        assertEquals(emptyReplacement, carRepo.takeData(1));

        assertTrue(carRepo.replace(emptyCar));

        emptyReplacement.setId(4);
        assertFalse(carRepo.replace(emptyReplacement));
        assertEquals(emptyCar, carRepo.takeData(1));
    }

    @Test
    void creationFromSource() throws IOException {
        assertAll(()->new CarRepository(Path.of("src/test/resources/testCarData.json")));
    }

    @Test
    void gettingFromSource() throws IOException {
        Repository<Car> repo = new CarRepository(Path.of("src/test/resources/testCarData.json"));
        assertEquals(new Car(1,"Audi",1,Year.of(2000),1), repo.takeData(1));
    }

    private AbstractRepository<Car> carRepo;
    private Car emptyCar;
    private Car firstCar;
    private Car secondCar;
    private Car thirdCar;

    /**
     * Initialise and reload repo
     */
    @BeforeEach
    public void initialize() throws IOException {
        Path pathToEmptyStudents = Path.of("src/test/resources/emptyCarRepo.json");
        String emptyContent = "[]";
        try (FileWriter fileWriter = new FileWriter(pathToEmptyStudents.toFile())) {
            fileWriter.write(emptyContent);
        }
        carRepo = new CarRepository(pathToEmptyStudents);
        emptyCar = new Car();
        firstCar = new Car(1, "Audi", 1, Year.of(2000), 1);
        secondCar = new Car(2, "BMW", 2, Year.of(2001), 2);
        thirdCar = new Car(3, "Reno", 3, Year.of(2002), 3);
    }

    /**
     * Fills repo with all the students starting from the empty one
     */
    private void fillRepo() {
        carRepo.add(emptyCar);
        carRepo.add(firstCar);
        carRepo.add(secondCar);
        carRepo.add(thirdCar);
    }
}