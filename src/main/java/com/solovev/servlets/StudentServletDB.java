package com.solovev.servlets;

import com.solovev.dto.ResponseResult;
import com.solovev.model.Student;
import com.solovev.repositories.StudentRepository;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;

@WebServlet("/students_DB")
public class StudentServletDB extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("utf-8");
        resp.setCharacterEncoding("utf-8");
        resp.setContentType("application/json;charset=utf-8");

        String idString = req.getParameter("id");
        try (StudentRepository repo = new StudentRepository()) {
            if (idString != null) {
                ResponseResult<Student> result = new ResponseResult<>();
                try {
                    int id = Integer.parseInt(idString);
                    Student foundStudent = repo.takeData(id);

                    if (foundStudent != null) {
                        result.setData(repo.takeData(id));
                    } else {
                        result.setMessage("Cannot find object with this ID");
                    }
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
    }
}
