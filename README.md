## Overview

This project is a servlet-based application designed for practicing various technologies while managing student and car data. It has two branches: "without_db," which stores data in JSON files, and "main," which integrates with a MySQL database. The application is built for deployment on Tomcat and includes testing using JUnit and Mockito.
This is sever part of the porject. 
Client part build with JavaFX can be found [here](https://github.com/SolovevAnton/Practice_Project_StudentsFX).

### Used tech
-	Apache Tomcat
-	Servlet API
-	MySQL DB
-	JDBC
-	Jackson
- JUnit5
- Mockito

### Architecture and Internal Structure
Servlet-Based Structure

    Servlets: The application leverages Java Servlet technology to handle HTTP requests and manage data operations.
    Deployment: To deploy this application, Tomcat, a widely-used servlet container, is utilized.
    Data Management: Data is managed through repositories, allowing storage in JSON files (in the "without_db" branch) or in a MySQL database (in the "main" branch).
    API Endpoints: Servlets define APIs for various operations such as adding, deleting, updating, and retrieving data.
    Response Handling: JSON objects are returned as responses in the "application/json" format.

## Usage
### API Usage

    Use HTTP requests to perform various operations on student and car data as demonstrated in the test cases.
    JSON Data: You can send data in JSON format by including it in the request body for POST and PUT operations.

### Response Format

All API responses are in JSON format and follow the structure:

json

{
  "message": "null message or error description",
  "data": "The requested data or null in case of an error"
}
### Deployment
    1. Copy project to your IDE
    2. Deploy this application on a Tomcat server.
  to test use your own queries or try [client part](https://github.com/SolovevAnton/Practice_Project_StudentsFX) for this project build in JavaFX
    
Feel free to explore the code.

Thank you for visiting my portfolio! Happy coding!

## Testing examples

Here are some usage test cases that demonstrate the capabilities of the application:
Retrieving Data

    GET /students?id=1
        Retrieves a specific student by ID (e.g., ID 1).

    GET /students?id=-1
        Attempts to retrieve a student with an invalid negative ID.

    GET /students?id=hi
        Attempts to retrieve a student with a non-integer ID.

    GET /students
        Retrieves all students.

Adding Data

    POST /students?name="fourth"&age=10&num=10&salary=100
        Adds a new student with specified parameters.

    POST /students?name="second"&age=10&num=10
        Attempts to add a student with missing parameters.

    POST /students?name="second"&age=10&num=l
        Attempts to add a student with a non-integer parameter.

    POST /students
        Adds a new student using JSON format in the request body.

Deleting Data

    DELETE /students
        Deletes all students.

    DELETE /students?id=0
        Attempts to delete a student with ID 0.

    DELETE /students?id=-1
        Attempts to delete a student with an invalid negative ID.

    DELETE /students?id=4
        Deletes a student with ID 4.

    DELETE /students?id=hey
        Attempts to delete a student with a non-integer ID.

Updating Data

    PUT /students?id=4&name=replaced&salary=2000
        Updates a student with ID 4 with specified parameters.

    PUT /students?id=4&name=replaced&salary=2000&age=90&num=90
        Updates a student with ID 4 with additional parameters.

    PUT /students?id=4
        Attempts to update a student with missing parameters.

    PUT /students?id=4&salary=2000l
        Attempts to update a student with an invalid parameter.

    PUT /students?id=-1&salary=2000l
        Attempts to update a student with an invalid negative ID.
