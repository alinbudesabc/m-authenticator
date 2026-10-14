package com.alinbudesabc.authenticator.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
public class TestController {


    @GetMapping
    public String testController(){
        return "Test Controller works";
    }

    @GetMapping("/basic")
    public String authenticatedApi(){
        return "Secured endpoint with basic authentication";
    }

    @GetMapping("/roleUser")
    public String roleAdmin(){
        return "User with role USER accessed the endpoint";
    }


}
