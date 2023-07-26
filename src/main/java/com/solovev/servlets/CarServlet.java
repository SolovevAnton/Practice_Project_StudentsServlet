package com.solovev.servlets;

import com.solovev.dto.ResponseResult;
import com.solovev.model.Car;
import com.solovev.repositories.CarRepository;
import com.solovev.repositories.Repository;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
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

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        config(req, resp);

        String stringId = req.getParameter("id");
        if (stringId != null) {
            try {
                int id = Integer.parseInt(stringId);
                Car carToReturn = repo.takeData(id);
                if(carToReturn != null){
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
}
