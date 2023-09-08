package com.solovev.servlets;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.solovev.dto.ResponseResult;
import com.solovev.model.Car;
import com.solovev.repositories.CarRepository;
import com.solovev.repositories.Repository;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collection;

@WebServlet("/cars")
public class CarServlet extends HttpServlet {

    private ResponseResult<Car> responseResult;
    private final String messageNoId = "Please provide object ID";
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    private String notFoundIdMessage(String id) {
        return "Cannot find object with this ID: " + id;
    }

    /**
     * Checks if request contains json or not
     *
     * @param req to check
     * @return true if is json false otherwise
     */
    private boolean isJson(HttpServletRequest req) {
        String header = req.getHeader("Content-Type");
        return header != null && header.contains("application/json");
    }

    /**
     * Configs resp and req to use UTF-8, also reloads repository
     *
     * @param req  to config
     * @param resp ro config
     */
    private void config(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("utf-8");
        resp.setCharacterEncoding("utf-8");
        resp.setContentType("application/json;charset=utf-8");
        responseResult = new ResponseResult<>();
    }

    /**
     * Creates car based on given parameters of the request, if they are presented
     *
     * @param req to take param from
     * @return modified student
     * @throws NumberFormatException                   if one of the parsed number parameters cannot be parsed
     * @throws java.time.format.DateTimeParseException if year will not be parsed correctly
     */
    private Car carCreator(HttpServletRequest req) throws NumberFormatException {
        Car car = new Car();
        if (req.getParameter("brand") != null) {
            car.setBrand(req.getParameter("brand"));
        }
        if (req.getParameter("power") != null) {
            car.setPower(Integer.parseInt(req.getParameter("power")));
        }
        if (req.getParameter("year") != null) {
            car.setYear(Year.parse(req.getParameter("year"), DateTimeFormatter.ofPattern("yyyy")));
        }
        if (req.getParameter("idStudent") != null) {
            car.setIdStudent(Integer.parseInt(req.getParameter("idStudent")));
        }
        return car;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        config(req, resp);
        //returns car or all if id null, else throws
        String stringId = req.getParameter("id");
        try (Repository<Car> repo = new CarRepository()){
            if (stringId != null) {
                try {
                    int id = Integer.parseInt(stringId);
                    Car carToReturn = repo.takeData(id);
                    if (carToReturn != null) {
                        responseResult.setData(carToReturn);
                    } else {
                        responseResult.setMessage(notFoundIdMessage(stringId));
                    }
                } catch (NumberFormatException e) {
                    responseResult.setMessage("Error: " + e);
                }
                resp.getWriter().write(responseResult.jsonToString());
            } else {
                ResponseResult<Collection<Car>> allCarsResponse = new ResponseResult<>();
                allCarsResponse.setData(repo.takeData());
                resp.getWriter().write(allCarsResponse.jsonToString());
            }
        }
    }

    /**
     * If there is object in the request it will be used to post, and all other parameters will be ignored;
     * Without any values will add empty car!
     *
     * @param req  request with parameters or with object; empty post request results in empty car addition
     * @param resp response with posted object is success with message otherwise
     * @throws IOException if IO exc occurs
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        config(req, resp);
        //returns posted car
        try(Repository<Car> repo = new CarRepository()) {
            Car carToAdd = isJson(req)
                    ? objectMapper.readValue(req.getReader(), Car.class)
                    : carCreator(req);

            if (repo.add(carToAdd)) {
                responseResult.setData(carToAdd);
            } else {
                responseResult.setMessage("Cannot add car since constraint violated in DB");
            }
        } catch (NumberFormatException | DateTimeParseException | JsonParseException e) {
            responseResult.setMessage("Error: " + e);
        }

        resp.getWriter().write(responseResult.jsonToString());
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        config(req, resp);
        String stringId = req.getParameter("id");
        //returns deleted car or throws
        if (stringId != null) {
            try (Repository<Car> repo = new CarRepository()){
                int id = Integer.parseInt(stringId);
                Car carDeleted = repo.delete(id);
                if (carDeleted != null) {
                    responseResult.setData(carDeleted);
                } else {
                    responseResult.setMessage(notFoundIdMessage(stringId));
                }
            } catch (NumberFormatException e) {
                responseResult.setMessage("Error: " + e);
            }

        } else {
            responseResult.setMessage(messageNoId);
        }
        resp.getWriter().write(responseResult.jsonToString());

    }

    /**
     * Updates student in the repo based on its id; if id is not provided car with id 1 will be replaced!
     * If it has json object all fields will be ignored;
     *
     * @param req  request must contain all fields with id
     * @param resp response will contain REPLACED object
     * @throws IOException if IO exc occurs
     */
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        config(req, resp);
        //returns replaced car or error msg
        String stringId = req.getParameter("id");
        boolean isJson = isJson(req);

        if (stringId != null || isJson) {
            try (Repository<Car> repo = new CarRepository()){
                Car carReplacement;
                int id;
                if (isJson) {
                    carReplacement = objectMapper.readValue(req.getReader(), Car.class);
                    id = carReplacement.getId();
                } else {
                    carReplacement = carCreator(req);
                    id = Integer.parseInt(stringId);
                }

                Car carReplaced = repo.takeData(id);
                carReplacement.setId(id);

                if (repo.replace(carReplacement)) {
                    responseResult.setData(carReplaced);
                } else {
                    responseResult.setMessage(notFoundIdMessage(String.valueOf(id)) + " or DB constrain violated");
                }
            } catch (NumberFormatException | DateTimeParseException | JsonParseException e) {
                responseResult.setMessage("Error: " + e);
            }
        } else {
            responseResult.setMessage(messageNoId);
        }
        resp.getWriter().write(responseResult.jsonToString());
    }
}
