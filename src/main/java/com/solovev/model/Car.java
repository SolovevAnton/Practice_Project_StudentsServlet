package com.solovev.model;

import java.time.Year;
import java.util.Objects;

public class Car implements IdHolder{
    private int id;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Car car)) return false;

        if (id != car.id) return false;
        if (power != car.power) return false;
        if (idStudent != car.idStudent) return false;
        if (!Objects.equals(brand, car.brand)) return false;
        return Objects.equals(year, car.year);
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (brand != null ? brand.hashCode() : 0);
        result = 31 * result + power;
        result = 31 * result + (year != null ? year.hashCode() : 0);
        result = 31 * result + idStudent;
        return result;
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
