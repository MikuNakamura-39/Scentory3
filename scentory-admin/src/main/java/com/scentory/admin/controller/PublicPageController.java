package com.scentory.admin.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PublicPageController {

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/password/reset")
    public String passwordReset() {
        return "password-reset";
    }

    @GetMapping("/password/new")
    public String passwordNew() {
        return "password-new";
    }

    @GetMapping("/terms")
    public String terms() {
        return "terms";
    }

    @GetMapping("/privacy")
    public String privacy() {
        return "privacy";
    }

    @GetMapping("/contact")
    public String contact() {
        return "contact";
    }
}
