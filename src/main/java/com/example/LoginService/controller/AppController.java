package com.example.LoginService.controller;

import com.example.LoginService.feignClient.FeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpSession;
import java.util.*;

@Controller
public class AppController {

    private FeignClient feignClient;

    private RestTemplate restTemplate;
    private Map map;

    @Autowired
    public AppController(FeignClient feignClient, RestTemplate restTemplate) {
        this.feignClient = feignClient;
        this.restTemplate = restTemplate;
    }

    // request url ->(GET) http://localhost:8074/home
    @GetMapping("/home")
    public String firstPage(){
        return "home";
    }

    // request url ->(GET) http://localhost:8074/welcomeAdmin
    @GetMapping("/welcomeAdmin")
    public String welcomeAdmin(HttpSession httpSession, Model model) {
        httpSession.setAttribute("user","admin");
        model.addAttribute("users","Admin");
        return "login";
    }

    // request url ->(GET) http://localhost:8074/welcomeAdmin
    @GetMapping("/welcomeStudent")
    public String welcomeStudent(HttpSession httpSession, Model model){
        httpSession.setAttribute("user","student");
        model.addAttribute("users", "Student");
        return "login";
    }


    // request url ->(GET) http://localhost:8074/login
    @GetMapping("/login")
    public String login(){
        return "login";
    }


    //request url ->(GET) http://localhost:8074/admin/admin_id/addAssignment
    @GetMapping("/addAssignment")
    public String addAssignment(){
        return "addAssignment";
    }


    //request url ->(GET) http://localhost:8074/admin/admin_id/students
    @GetMapping("/students")
    public String students(HttpSession httpSession, Model model){

        // Getting student name and assignment's of admin
        String assignments = feignClient.getAdminAssignment((String)httpSession.getAttribute("id"));
        String students = feignClient.getStudentsName((String)httpSession.getAttribute("id"));

        // converting string into list and map object

        List<String> str2 = new ArrayList<>();
        Map<String,String> studentIdName = new HashMap<>();

        try {

            String str1[] = assignments.split("\\[|]");
            str2 = Arrays.asList(str1[1].split("\",\"|\""));
        }catch (Exception e){}

        try{
            // Converting string into map
            String student[] = students.split("\\{|}");
            String id_name[] = student[1].split(",");

            for (String std: id_name) {
                String str[] = std.split("\"");
                studentIdName.put(str[1],str[3]);
            }

        }catch (Exception e){}

        model.addAttribute("allAssignment",str2);
        model.addAttribute("studentsName",studentIdName);

        return "students";
    }


    //request url ->(GET) http://localhost:8074/student/student_id/back
    @GetMapping("/student/{id}/back")
    public String goBack(){
        return "redirect:/students";
    }


    //request url ->(GET) http://localhost:8074/student/student_id/logout
    @GetMapping("/student/{id}/logout")
    public String logout(){
        return "redirect:/home";
    }


    //request url ->(GET) http://localhost:8074/logout
    @GetMapping("/logout")
    public String adminLogout(){
        return "redirect:/home";
    }

    // request url ->(GET)  http://localhost:8074/student/student_id/addStudentAssignment
    @GetMapping("/student/{id}/addStudentAssignment")
    public String addStudentAssignment( HttpSession httpSession, Model model){

        List<String> assignments = getAssignments(httpSession);
        model.addAttribute("assignments",assignments);
        return "allAssignments";
    }

    // request url ->(GET)  http://localhost:8074/student/student_id/assignments
    @GetMapping("/student/{id}/assignments")
    public String getStudentAssignment(@PathVariable("id")String id, Model model, HttpSession httpSession) {

        // Getting student's assigned assignments and completed assignment
        String assignment = feignClient.getStudentAssignment(id);
        String completeAssignment = feignClient.getStudentCompleteAssignment(id);

        String str1[] = assignment.split("\\[|]");
        String str2[] = completeAssignment.split("\\[|]");

        // convert string into list object of string
        List<String> assignments = new ArrayList<>();
        List<String> completeAssignments = new ArrayList<>();
        try {
             assignments = Arrays.asList(str1[1].split("\",\"|\""));
        }catch (Exception e){}
        try{
            completeAssignments = Arrays.asList(str2[1].split("\",\"|\""));
        }catch (Exception e){}

        model.addAttribute("assignments", assignments);
        model.addAttribute("completeAssignments", completeAssignments);

        if(httpSession.getAttribute("user").equals("admin")){
            return "adminAssignments";
        }
        return "assignments";
    }

    // request url ->(GET)  http://localhost:8074/student/student_id/assignment/assignment_for_student
    @GetMapping("/student/{id}/assignment/{assignment}")
    public String addStudentAssignment(@PathVariable("id")String id,
                                       @PathVariable("assignment")String assignment, HttpSession httpSession){

        // adding a new assignment ot student's assignment
        feignClient.addStudentAssignment(id,assignment);
        return "redirect:/student/"+id+"/assignments";
    }

    // request url ->(GET)  http://localhost:8074/student/student_id/addCompleteAssignment
    @GetMapping("/student/{id}/addCompleteAssignment")
    public String addCompleteAssignment(@PathVariable("id")String id, HttpSession httpSession, Model model){

        // Getting assignment to student's complete assignment
        String assignments = feignClient.getStudentAssignment(id);

        // converting string into list object of string
        String str1[] = assignments.split("\\[|]");
        List<String> str2 = new ArrayList<>();
        try {
            str2 = Arrays.asList(str1[1].split("\",\"|\""));
        }catch (Exception e){}

        model.addAttribute("assignments", str2);
        return "addCompleteAssignments";
    }

    // request url ->(GET)  http://localhost:8074/student/student_id/addCompleteAssignment/assignment_for_student
    @GetMapping("/student/{id}/addCompleteAssignment/{assignment}")
    public String addCompletedAssignment(@PathVariable("id")String id, @PathVariable("assignment")String assignment){

        // Adding a new assignment to student's complete assignmnet

        feignClient.addStudentCompleteAssignment(id,assignment);
        return "redirect:/student/"+id+"/assignments";
    }


    // Post Mappings


    // request url ->(POST)  http://localhost:8074/admin/admin_id/addAssignment
    @PostMapping("/addAssignment")
    public String addNewAssignment(@RequestParam("assignment")String assignment, HttpSession httpSession){

        // Adding assignment to admins' assignment
        feignClient.addAdminAssignment((String)httpSession.getAttribute("id"),assignment);
        return "redirect:students";
    }

    // request url ->(POST)  http://localhost:8074/login
    @PostMapping("/login")
    public String login(@RequestParam("email")String email, @RequestParam("password")String password,
                        HttpSession httpSession, Model model){

        String login;
        // checking user is admin or student using session attribute
        if(httpSession.getAttribute("user").equals("admin")){
            System.out.println("done");

            // checking email id and password
            // using feignClient
            login = feignClient.checkAdminLogin(email,password);

            if(login.equals("true")){
                httpSession.setAttribute("id",email);
                return "redirect:students";
            }else{
                return "redirect:login";
            }
        }else{

            // check  login using Database Microservice
            login = feignClient.checkStudentLogin(email,password);

            if(login.equals("true")){
                return "redirect:/student/"+email+"/assignments";

            }else{
                return "redirect:login";
            }
        }
    }


    // functions

    public List<String> getAssignments(HttpSession httpSession){

        String assignments = feignClient.getAdminAssignment((String)httpSession.getAttribute("id"));

        String str1[] = assignments.split("\\[|]");
        List<String> str2 = new ArrayList<>();
        try {
            str2 = Arrays.asList(str1[1].split("\",\"|\""));
        }catch (Exception e){}
        return  str2;
    }

}
