package com.example.Spot.user.presentation.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@ResponseBody
public class MasterController {

    @GetMapping("/api/master")
    public String masterP() {

        return "master Controller";
    }
}
