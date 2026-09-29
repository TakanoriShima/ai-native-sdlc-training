package com.example.salesmanagement.auth.controller;

import com.example.salesmanagement.auth.service.AppUserPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal AppUserPrincipal principal, Model model) {
        model.addAttribute("name", principal.getName());
        model.addAttribute("role", principal.getRole());
        return "dashboard";
    }
}
