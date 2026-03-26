package com.scentory.admin.controller;

import jakarta.servlet.http.HttpServletRequest;
import java.security.Principal;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAttributes {

    @ModelAttribute("isAdmin")
    public boolean isAdmin(Principal principal) {
        return hasRole(principal, "ROLE_ADMIN");
    }

    @ModelAttribute("isStaff")
    public boolean isStaff(Principal principal) {
        return hasRole(principal, "ROLE_STAFF");
    }

    @ModelAttribute("isViewer")
    public boolean isViewer(Principal principal) {
        return hasRole(principal, "ROLE_VIEWER");
    }

    @ModelAttribute("currentPath")
    public String currentPath(HttpServletRequest request) {
        return request.getRequestURI();
    }

    private boolean hasRole(Principal principal, String role) {
        if (!(principal instanceof org.springframework.security.core.Authentication authentication)) {
            return false;
        }
        return authentication.getAuthorities().stream()
            .anyMatch(authority -> authority.getAuthority().equals(role));
    }
}
