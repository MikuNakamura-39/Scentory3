package com.scentory.admin.controller;

import com.scentory.admin.entity.Reservation;
import com.scentory.admin.model.ReservationStatus;
import com.scentory.admin.repository.ReservationRepository;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReservationRepository reservationRepository;

    @GetMapping
    public String index(Model model) {
        Map<ReservationStatus, Long> byStatus = reservationRepository.findAll().stream()
            .collect(Collectors.groupingBy(Reservation::getStatus, Collectors.counting()));
        Map<String, Long> byChannel = reservationRepository.findAll().stream()
            .collect(Collectors.groupingBy(Reservation::getChannel, Collectors.counting()));
        model.addAttribute("pageTitle", "レポート・集計");
        model.addAttribute("byStatus", byStatus);
        model.addAttribute("byChannel", byChannel);
        return "reports/index";
    }
}
