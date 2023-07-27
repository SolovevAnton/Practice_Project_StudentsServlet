package com.solovev.servlets;

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

@WebServlet("/car")
public class CarServlet extends HttpServlet {
    private Repository<Car> repo;
    private ResponseResult<Car> responseResult;
    private final String notFoundIdMessage = "Cannot find object with this ID";

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
        repo = new CarRepository();
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
        if (stringId != null) {
            try {
                int id = Integer.parseInt(stringId);
                Car carToReturn = repo.takeData(id);
                if (carToReturn != null) {
                    responseResult.setData(carToReturn);
                } else {
                    responseResult.setMessage(notFoundIdMessage);
                }
            } catch (NumberFormatException e) {
                responseResult.setMessage(e.toString());
            }
            resp.getWriter().write(responseResult.jsonToString());
        } else {
            ResponseResult<Collection<Car>> allCarsResponse = new ResponseResult<>();
            allCarsResponse.setData(repo.takeData());
            resp.getWriter().write(allCarsResponse.jsonToString());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        config(req, resp);
        //returns posted car

        try {
            Car carToAdd = carCreator(req);
            if (repo.add(carToAdd)) {
                responseResult.setData(carToAdd);
            } else {
                responseResult.setMessage("This object already exists in database");
            }
        } catch (NumberFormatException | DateTimeParseException e) {
            responseResult.setMessage(e.toString());
        }
        resp.getWriter().write(responseResult.jsonToString());
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        config(req, resp);
        String stringId = req.getParameter("id");
        //returns deleted car or throws
        if (stringId != null) {
            try {
                int id = Integer.parseInt(stringId);
                Car carDeleted = repo.delete(id);
                if (carDeleted != null) {
                    responseResult.setData(carDeleted);
                } else {
                    responseResult.setMessage(notFoundIdMessage);
                }
            } catch (NumberFormatException e) {
                responseResult.setMessage(e.toString());
            }
            resp.getWriter().write(responseResult.jsonToString());
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        config(req, resp);
        //returns replaced car or error msg
        String stringId = req.getParameter("id");
        if (stringId != null) {
            try {
                Car carReplacement = carCreator(req);
                int id = Integer.parseInt(stringId);
                Car carReplaced = repo.takeData(id);
                carReplacement.setId(id);

                if (repo.replace(carReplacement)) {
                    responseResult.setData(carReplaced);
                } else {
                    responseResult.setMessage(notFoundIdMessage);
                }
            } catch (NumberFormatException | DateTimeParseException e) {
                responseResult.setMessage(e.toString());
            }
            resp.getWriter().write(responseResult.jsonToString());
        }
    }
}
