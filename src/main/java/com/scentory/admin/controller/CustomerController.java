package com.scentory.admin.controller;

import com.scentory.admin.entity.Customer;
import com.scentory.admin.repository.CustomerRepository;
import com.scentory.admin.repository.ReservationRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerRepository customerRepository;
    private final ReservationRepository reservationRepository;

    @GetMapping
    public String list(@RequestParam(required = false) String keyword, Model model) {
        model.addAttribute("pageTitle", "顧客管理");
        model.addAttribute("keyword", keyword);
        model.addAttribute("customers", keyword == null || keyword.isBlank()
            ? customerRepository.findAll()
            : customerRepository.findAll().stream()
                .filter(customer -> containsIgnoreCase(customer.getFullName(), keyword)
                    || containsIgnoreCase(customer.getEmail(), keyword)
                    || containsIgnoreCase(customer.getPhone(), keyword))
                .toList());
        return "customers/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("pageTitle", "顧客登録");
        model.addAttribute("customer", new Customer());
        return "customers/form";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Customer customer = customerRepository.findById(id).orElseThrow();
        model.addAttribute("pageTitle", "顧客詳細");
        model.addAttribute("customer", customer);
        model.addAttribute("reservations", reservationRepository.findByCustomerIdOrderByCreatedAtDesc(id));
        return "customers/detail";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("pageTitle", "顧客編集");
        model.addAttribute("customer", customerRepository.findById(id).orElseThrow());
        return "customers/form";
    }

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute Customer customer, BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("pageTitle", customer.getId() == null ? "顧客登録" : "顧客編集");
            return "customers/form";
        }
        customerRepository.save(customer);
        redirectAttributes.addFlashAttribute("message", "顧客を保存しました。");
        return "redirect:/customers";
    }

    private boolean containsIgnoreCase(String value, String keyword) {
        return value != null && value.toLowerCase().contains(keyword.toLowerCase());
    }
}
