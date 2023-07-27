package com.solovev.model;

import java.time.Year;
import java.util.Objects;

public class Car implements IdHolder {
    private int id = 1; //id starts from 1 in repo so when empty is added it is not NULL!
    private String brand;
    private int power;
    private Year year;
    private int idStudent;

    public Car() {
    }

    public Car(int id, String brand, int power, Year year, int idStudent) {
        this.id = id;
        this.brand = brand;
        this.power = power;
        this.year = year;
        this.idStudent = idStudent;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public int getPower() {
        return power;
    }

    public void setPower(int power) {
        this.power = power;
    }

    public Year getYear() {
        return year;
    }

    public void setYear(Year year) {
        this.year = year;
    }

    public int getIdStudent() {
        return idStudent;
    }

    public void setIdStudent(int idStudent) {
        this.idStudent = idStudent;
    }

    /**
     * NOTE: ID does not included in equals, since it computes out of the object
     *
     * @param o object to compare
     * @return true if objects are logically equal (EXCEPT ID FIELD) false otherwise
     */

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Car car = (Car) o;
        return power == car.power && idStudent == car.idStudent && Objects.equals(brand, car.brand) && Objects.equals(year, car.year);
    }

    @Override
    public int hashCode() {
        return Objects.hash(brand, power, year, idStudent);
    }

    @Override
    public String toString() {
        return "Car{" +
                "id=" + id +
                ", brand='" + brand + '\'' +
                ", power=" + power +
                ", year=" + year +
                ", idStudent=" + idStudent +
                '}';
    }
}
