package com.scentory.admin.controller;

import com.scentory.admin.entity.Staff;
import com.scentory.admin.model.RoleType;
import com.scentory.admin.repository.StaffRepository;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/staff")
@RequiredArgsConstructor
public class StaffController {

    private final StaffRepository staffRepository;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("pageTitle", "スタッフ管理");
        model.addAttribute("staffs", staffRepository.findAll());
        return "staff/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("pageTitle", "スタッフ登録");
        model.addAttribute("staff", new Staff());
        model.addAttribute("roles", RoleType.values());
        return "staff/form";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("pageTitle", "スタッフ編集");
        model.addAttribute("staff", staffRepository.findById(id).orElseThrow());
        model.addAttribute("roles", RoleType.values());
        return "staff/form";
    }

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute Staff staff, BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("roles", RoleType.values());
            model.addAttribute("pageTitle", staff.getId() == null ? "スタッフ登録" : "スタッフ編集");
            return "staff/form";
        }
        staffRepository.save(staff);
        redirectAttributes.addFlashAttribute("message", "スタッフを保存しました。");
        return "redirect:/staff";
    }
}
