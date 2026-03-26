package com.scentory.admin.controller;

import com.scentory.admin.service.DashboardService;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping({"/", "/dashboard"})
    public String dashboard(Model model) {
        LocalDate today = LocalDate.now();
        model.addAttribute("pageTitle", "ダッシュボード");
        model.addAttribute("dashboardMonthLabel", today.getYear() + "年" + today.getMonthValue() + "月");
        model.addAttribute("todayReservations", dashboardService.todayReservations());
        model.addAttribute("availableSlots", dashboardService.availableSlots());
        model.addAttribute("reservationCount", dashboardService.reservationCount());
        model.addAttribute("cancellationRate", dashboardService.cancellationRate());
        model.addAttribute("revenue", dashboardService.revenue());
        model.addAttribute("notices", dashboardService.notices());
        return "dashboard";
    }
}
