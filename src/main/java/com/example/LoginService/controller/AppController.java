package com.example.LoginService.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpSession;
import java.util.*;

@Controller
@SessionAttributes("user")
public class AppController {

    private RestTemplate restTemplate;
    private Map map;

    @Autowired
    public AppController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    // request url ->(GET) http://localhost:8074/home
    @GetMapping("/home")
    public String firstPage(){
        return "home";
    }

    // request url ->(GET) http://localhost:8074/welcomeAdmin
    @GetMapping("welcomeAdmin")
    public String welcomeAdmin(HttpSession httpSession, Model model) {
        httpSession.setAttribute("user","admin");
        model.addAttribute("users","Admin");
        return "login";
    }

    // request url ->(GET) http://localhost:8074/welcomeAdmin
    @GetMapping("welcomeStudent")
    public String welcomeStudent(HttpSession httpSession, Model model){
        httpSession.setAttribute("user","student");
        model.addAttribute("users", "Student");
        return "login";
    }


    // request url ->(GET) http://localhost:8074/login
    @GetMapping("login")
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
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        HttpEntity<String> entity = new HttpEntity<String>(headers);

        // Getting student name and assignment's of admin
        String assignments = restTemplate.exchange("http://localhost:8072/admin/"+httpSession.getAttribute("id")+"/assignments", HttpMethod.GET, entity, String.class).getBody();
        String students = restTemplate.exchange("http://localhost:8072/admin/"+httpSession.getAttribute("id")+"/students", HttpMethod.GET, entity, String.class).getBody();

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

        // Add into model attribute
        try{
            System.out.println(str2.toString());
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
    @GetMapping("student/{id}/addStudentAssignment")
    public String addStudentAssignment(HttpSession httpSession, Model model){
        List<String> assignments = getAssignments(httpSession);
        model.addAttribute("assignments",assignments);
        return "allAssignments";
    }

    // request url ->(GET)  http://localhost:8074/student/student_id/assignments
    @GetMapping("/student/{id}/assignments")
    public String getStudentAssignment(@PathVariable("id")String id, Model model, HttpSession httpSession) {

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        HttpEntity<String> entity = new HttpEntity<String>(headers);

        // Getting student's assigned assignments and completed assignment
        String assignment = restTemplate.exchange("http://localhost:8072/student/"+id+"/assignments", HttpMethod.GET, entity, String.class).getBody();
        String completeAssignment = restTemplate.exchange("http://localhost:8072/student/"+id+"/completeAssignments", HttpMethod.GET, entity, String.class).getBody();

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
    @GetMapping("student/{id}/assignment/{assignment}")
    public String addStudentAssignment(@PathVariable("id")String id,
                                       @PathVariable("assignment")String assignment, HttpSession httpSession){
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        HttpEntity<String> entity = new HttpEntity<String>(headers);

        // adding a new assignment ot student's assignment
        restTemplate.exchange("http://localhost:8072/student/"+id+"/addAssignment?assignment="+assignment, HttpMethod.POST, entity, String.class).getBody();
        return "redirect:/student/"+id+"/assignments";
    }

    // request url ->(GET)  http://localhost:8074/student/student_id/addCompleteAssignment
    @GetMapping("student/{id}/addCompleteAssignment")
    public String addCompleteAssignment(@PathVariable("id")String id, HttpSession httpSession, Model model){
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        HttpEntity<String> entity = new HttpEntity<String>(headers);

        // Getting assignment to student's complete assignment
        String assignments = restTemplate.exchange("http://localhost:8072/student/"+id+"/assignments", HttpMethod.GET, entity, String.class).getBody();

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
    @GetMapping("student/{id}/addCompleteAssignment/{assignment}")
    public String addCompletedAssignment(@PathVariable("id")String id, @PathVariable("assignment")String assignment){

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        HttpEntity<String> entity = new HttpEntity<String>(headers);

        // Adding a new assignment to student's complete assignmnet
        restTemplate.exchange("http://localhost:8072/student/"+id+"/addCompleteAssignment?assignment="+assignment, HttpMethod.POST, entity, String.class).getBody();

        return "redirect:/student/"+id+"/assignments";
    }


    // Post Mappings


    // request url ->(POST)  http://localhost:8074/admin/admin_id/addAssignment
    @PostMapping("/addAssignment")
    public String addNewAssignment(@RequestParam("assignment")String assignment, HttpSession httpSession){
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        HttpEntity<String> entity = new HttpEntity<String>(headers);

        // Adding assignment to admins' assignment
        restTemplate.exchange("http://localhost:8072/admin/"+httpSession.getAttribute("id")+"/addAssignment?assignment="+assignment, HttpMethod.POST, entity, String.class).getBody();
        return "redirect:students";
    }

    // request url ->(POST)  http://localhost:8074/login
    @PostMapping("login")
    public String login(@RequestParam("email")String email, @RequestParam("password")String password,
                        HttpSession httpSession, Model model){

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        HttpEntity<String> entity = new HttpEntity<String>(headers);

        String login;

        // checking user is admin or student using session attribute
        if(httpSession.getAttribute("user").equals("admin")){
            System.out.println("done");

            // checking email id and password
            login = restTemplate.exchange("http://localhost:8072/admin/login?id="+email+"&password="+password, HttpMethod.POST, entity, String.class).getBody();
            System.out.println(login);
            if(login.equals("true")){
                httpSession.setAttribute("id",email);
                return "redirect:students";
            }else{
                return "redirect:login";
            }
        }else{

            // check  login using Database Microservice
            login = restTemplate.exchange("http://localhost:8072/student/login?id="+email+"&password="+password, HttpMethod.POST, entity, String.class).getBody();
            if(login.equals("true")){
                return "redirect:/student/"+email+"/assignments";

            }else{
                return "redirect:login";
            }
        }
    }


    // functions

    public List<String> getAssignments(HttpSession httpSession){
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        HttpEntity<String> entity = new HttpEntity<String>(headers);

        String assignments = restTemplate.exchange("http://localhost:8072/admin/"+httpSession.getAttribute("id")+"/assignments", HttpMethod.GET, entity, String.class).getBody();

        String str1[] = assignments.split("\\[|]");
        List<String> str2 = new ArrayList<>();
        try {
            str2 = Arrays.asList(str1[1].split("\",\"|\""));
        }catch (Exception e){}
        return  str2;
    }

}
