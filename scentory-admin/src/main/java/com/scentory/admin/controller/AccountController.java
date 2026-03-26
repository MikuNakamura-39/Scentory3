package com.scentory.admin.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/account")
public class AccountController {

    @GetMapping
    public String index(Authentication authentication, Model model) {
        model.addAttribute("pageTitle", "アカウント");
        model.addAttribute("username", authentication.getName());
        return "account/index";
    }
}
