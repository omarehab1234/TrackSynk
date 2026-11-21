package com.yourapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/companies")
public class CompanyPageController {

    @GetMapping("/choose")
    public String chooseCompany() {
        return "company/choose";
    }

    @GetMapping("/create")
    public String createCompanyPage() {
        return "company/create";
    }

    @GetMapping("/join")
    public String joinCompanyPage() {
        return "company/join";
    }
}