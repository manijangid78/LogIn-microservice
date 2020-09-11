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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@SessionAttributes("user")
public class AppController {

    private RestTemplate restTemplate;
    private Map map;

    @Autowired
    public AppController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @GetMapping("home")
    public String firstPage(){
        return "home";
    }

    @GetMapping("welcomeAdmin")
    public String welcomeAdmin(HttpSession httpSession) {
        httpSession.setAttribute("user", "admin");
        return "login";
    }

    @GetMapping("welcomeStudent")
    public String welcomeStudent(HttpSession httpSession){
        httpSession.setAttribute("user","student");
        return "login";
    }

    @GetMapping("login")
    public String login(){
        return "login";
    }

    @GetMapping("/addAssignment")
    public String addAssignment(){
        return "addAssignment";
    }

    @GetMapping("/students")
    public String students(HttpSession httpSession, Model model){
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        HttpEntity<String> entity = new HttpEntity<String>(headers);

        String assignments = restTemplate.exchange("http://localhost:8072/admin/"+httpSession.getAttribute("id")+"/assignments", HttpMethod.GET, entity, String.class).getBody();
        String students = restTemplate.exchange("http://localhost:8072/admin/"+httpSession.getAttribute("id")+"/students", HttpMethod.GET, entity, String.class).getBody();

        String str1[] = assignments.split("\\[|]");
        List<String> str2 = Arrays.asList(str1[1].split("\",\"|\""));

        // Converting string into map
        String student[] = students.split("\\{|}");
        String id_name[] = student[1].split(",");

        Map<String,String> studentIdName = new HashMap<>();

        for (String std: id_name) {
            String str[] = std.split("\"");
            studentIdName.put(str[1],str[3]);
        }

        model.addAttribute("allAssignment",str2);
        model.addAttribute("studentsName",studentIdName);

        return "students";
    }

    @GetMapping("student/{id}/addStudentAssignment")
    public String addStudentAssignment(HttpSession httpSession, Model model){
        List<String> assignments = getAssignments(httpSession);
        model.addAttribute("assignments",assignments);
        return "allAssignments";
    }

    // login password url :http://localhost:8074/login  (POST request)
    @PostMapping("login")
    public String login(@RequestParam("email")String email, @RequestParam("password")String password,
                        HttpSession httpSession, Model model){

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        HttpEntity<String> entity = new HttpEntity<String>(headers);

        String login;

        if(httpSession.getAttribute("user").equals("admin")){
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

    @GetMapping("/student/{id}/assignments")
    public String getStudentAssignement(@PathVariable("id")String id, Model model, HttpSession httpSession) {

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        HttpEntity<String> entity = new HttpEntity<String>(headers);

        String assignment = restTemplate.exchange("http://localhost:8072/student/"+id+"/assignments", HttpMethod.GET, entity, String.class).getBody();
        String completeAssignment = restTemplate.exchange("http://localhost:8072/student/"+id+"/completeAssignments", HttpMethod.GET, entity, String.class).getBody();

        String str1[] = assignment.split("\\[|]");
        String str2[] = completeAssignment.split("\\[|]");

        // convert string into list of string
        List<String> assignments = Arrays.asList(str1[1].split("\",\"|\""));
        List<String> completeAssignments = Arrays.asList(str2[1].split("\",\"|\""));

        model.addAttribute("assignments", assignments);
        model.addAttribute("completeAssignments", completeAssignments);
        return "assignments";
    }

    @PostMapping("/addAssignment")
    public String addNewAssignment(@RequestParam("assignment")String assignment, HttpSession httpSession){
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        HttpEntity<String> entity = new HttpEntity<String>(headers);

        restTemplate.exchange("http://localhost:8072/admin/"+httpSession.getAttribute("id")+"/addAssignment?assignment="+assignment, HttpMethod.POST, entity, String.class).getBody();
        return "redirect:students";
    }

    @GetMapping("student/{id}/assignment/{assignment}")
    public String addStudentAssignment(@PathVariable("id")String id,
                                       @PathVariable("assignment")String assignment, HttpSession httpSession){
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        HttpEntity<String> entity = new HttpEntity<String>(headers);
        restTemplate.exchange("http://localhost:8072/student/"+id+"/addAssignment?assignment="+assignment, HttpMethod.POST, entity, String.class).getBody();
        return "redirect:/student/"+id+"/assignments";
    }

    public List<String> getAssignments(HttpSession httpSession){
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        HttpEntity<String> entity = new HttpEntity<String>(headers);

        String assignments = restTemplate.exchange("http://localhost:8072/admin/"+httpSession.getAttribute("id")+"/assignments", HttpMethod.GET, entity, String.class).getBody();

        String str1[] = assignments.split("\\[|]");
        List<String> str2 = Arrays.asList(str1[1].split("\",\"|\""));
        return  str2;
    }

}
