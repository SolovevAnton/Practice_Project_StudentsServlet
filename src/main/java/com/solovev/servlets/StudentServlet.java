package com.solovev.servlets;

import com.solovev.dto.ResponseResult;
import com.solovev.model.Student;
import com.solovev.repositories.Repository;
import com.solovev.repositories.StudentRepository;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;

@WebServlet("/students")
public class StudentServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("utf-8");
        resp.setCharacterEncoding("utf-8");
        resp.setContentType("application/json;charset=utf-8");
        String idString = req.getParameter("id");
        Repository<Student> repo = new StudentRepository(); //toDo problem with file! How top go around?

        if (idString != null) {
            ResponseResult<Student> result = new ResponseResult<>();
            try {
                int id = Integer.parseInt(idString);
                result.setData(repo.takeData(id));
            } catch (NumberFormatException e) {
                result.setMessage(e + " Id must be an integer");
            }
            resp.getWriter().write(result.jsonToString());
        } else {
            ResponseResult<Collection<Student>> result = new ResponseResult<>();
            result.setData(repo.takeData());
            resp.getWriter().write(result.jsonToString());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("utf-8");
        resp.setCharacterEncoding("utf-8");
        resp.setContentType("application/json;charset=utf-8");
        Repository<Student> repo = new StudentRepository();
        ResponseResult<Boolean> responseResult = new ResponseResult<>();

        try {
            //if successfully created and added returns true, false otherwise
            responseResult.setData(repo.add(studentModifier(req, new Student())));
        } catch (NumberFormatException e) {
            responseResult.setMessage(e.toString());
        }
        resp.getWriter().write(responseResult.jsonToString());
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doPut(req, resp);
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doDelete(req, resp);
    }

    /**
     * Modifies student based on given parameters of the request, if they are presented
     *
     * @param req     to take param from
     * @param student to modify
     * @return modified student
     * @throws NumberFormatException if one of the parsed number parameters cannot be parsed
     */
    private Student studentModifier(HttpServletRequest req, Student student) throws NumberFormatException {
        if (req.getParameter("name") != null) {
            student.setName(req.getParameter("name"));
        }

        if (req.getParameter("age") != null) {
            student.setAge(Integer.parseInt(req.getParameter("age")));
        }
        if (req.getParameter("num") != null) {
            student.setNum(Long.parseLong(req.getParameter("num")));
        }
        if (req.getParameter("salary") != null) {
            student.setSalary(Integer.parseInt(req.getParameter("salary")));
        }
        return student;
    }
}
