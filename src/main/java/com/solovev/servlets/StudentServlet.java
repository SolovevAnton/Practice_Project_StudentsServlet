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

@WebServlet("/students")
public class StudentServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("utf-8");
        resp.setCharacterEncoding("utf-8");
        resp.setContentType("application/json;charset=utf-8");

        String idString = req.getParameter("id");
        Repository<Student> repo = new StudentRepository();

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

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("utf-8");
        resp.setCharacterEncoding("utf-8");
        resp.setContentType("application/json;charset=utf-8");
        Repository<Student> repo = new StudentRepository();
        ResponseResult<Student> responseResult = new ResponseResult<>();

        try {
            Student studentToAdd = studentModifier(req);
            if (repo.add(studentToAdd)) {
                responseResult.setData(studentToAdd);
            } else {
                responseResult.setMessage("This object already exists in database");
            }
        } catch (NumberFormatException e) {
            responseResult.setMessage(e.toString());
        }
        resp.getWriter().write(responseResult.jsonToString());
    }

    /**
     * Updates student in the repo based on its id
     *
     * @param req  request must contain all filds with id
     * @param resp response will contain REPLACED object
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("utf-8");
        resp.setCharacterEncoding("utf-8");
        resp.setContentType("application/json;charset=utf-8");

        //us object was found and updated returns old object else throws

        ResponseResult<Student> responseResult = new ResponseResult<>();
        String idString = req.getParameter("id");

        if (idString != null) {
            try {
                Repository<Student> repo = new StudentRepository();
                int id = Integer.parseInt(idString);

                Student studentToCreate = studentModifier(req);
                studentToCreate.setId(id);

                Student studentToReplace = repo.takeData(id);

                if (repo.replace(studentToCreate)) {
                    responseResult.setData(studentToReplace);
                } else {
                    responseResult.setMessage("Cannot find object with this ID");
                }
            } catch (NumberFormatException e) {
                responseResult.setMessage(e.toString());
            }
        }
        resp.getWriter().write(responseResult.jsonToString());
    }


    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("utf-8");
        resp.setCharacterEncoding("utf-8");
        resp.setContentType("application/json;charset=utf-8");

        //if successfully deleted returns true, false otherwise
        ResponseResult<Student> responseResult = new ResponseResult<>();
        String idString = req.getParameter("id");

        if (idString != null) {
            try {
                Repository<Student> repo = new StudentRepository();
                int id = Integer.parseInt(idString);
                Student student = repo.delete(id);
                if (student != null) {
                    responseResult.setData(student);
                } else {
                    responseResult.setMessage("Cannot find object with this ID");
                }
            } catch (NumberFormatException e) {
                responseResult.setMessage(e + " Id must be an integer");
            }
        } else {
            responseResult.setMessage("Please provide object ID");
        }
        resp.getWriter().write(responseResult.jsonToString());
    }

    /**
     * Modifies student based on given parameters of the request, if they are presented
     *
     * @param req     to take param from
     * @return modified student
     * @throws NumberFormatException if one of the parsed number parameters cannot be parsed
     */
    private Student studentModifier(HttpServletRequest req) throws NumberFormatException {
        Student student = new Student();
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
