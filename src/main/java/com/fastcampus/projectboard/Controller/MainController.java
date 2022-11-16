package com.fastcampus.projectboard.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class MainController {

    @GetMapping("/")
    public String root(){
        return "forward:/articles";
    }

    @GetMapping("/null")
    public String nullPage(){
        return "redirect:/";
    }

}
