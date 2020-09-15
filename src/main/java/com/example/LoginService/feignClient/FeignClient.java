package com.example.LoginService.feignClient;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@org.springframework.cloud.openfeign.FeignClient("database-service")
public interface FeignClient {

    @PostMapping("db-api/admin/login")
    String checkAdminLogin(@RequestParam("id")String id, @RequestParam("password")String password);

    @PostMapping("db-api/student/login")
    String checkStudentLogin(@RequestParam("id")String id, @RequestParam("password")String password);

    @GetMapping("db-api/admin/{id}/assignments")
    String getAdminAssignment(@PathVariable("id")String id);

    @GetMapping("db-api/admin/{id}/students")
    String getStudentsName(@PathVariable("id")String id);

    @GetMapping("db-api/student/{id}/assignments")
    String getStudentAssignment(@PathVariable("id")String id);

    @GetMapping("db-api/student/{id}/completeAssignments")
    String getStudentCompleteAssignment(@PathVariable("id")String id);

    @PostMapping("db-api/student/{id}/addAssignment")
    String addStudentAssignment(@PathVariable("id")String id,@RequestParam("assignment")String assignment);

    @PostMapping("db-api/student/{id}/addCompleteAssignment")
    String addStudentCompleteAssignment(@PathVariable("id")String id, @RequestParam("assignment")String assignment);

    @PostMapping("db-api/admin/{id}/addAssignment")
    String addAdminAssignment(@PathVariable("id")String id, @RequestParam("assignment")String Assignment);

}
