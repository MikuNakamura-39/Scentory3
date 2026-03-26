package com.scentory.admin.controller;

import com.scentory.admin.entity.WorkshopMenu;
import com.scentory.admin.repository.WorkshopMenuRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/menus")
@RequiredArgsConstructor
public class MenuController {

    private final WorkshopMenuRepository workshopMenuRepository;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("pageTitle", "商品・メニュー管理");
        model.addAttribute("menus", workshopMenuRepository.findAll());
        return "menus/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("pageTitle", "コース登録");
        model.addAttribute("menu", new WorkshopMenu());
        return "menus/form";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("pageTitle", "コース編集");
        model.addAttribute("menu", workshopMenuRepository.findById(id).orElseThrow());
        return "menus/form";
    }

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("menu") WorkshopMenu menu, BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("pageTitle", menu.getId() == null ? "コース登録" : "コース編集");
            return "menus/form";
        }
        workshopMenuRepository.save(menu);
        redirectAttributes.addFlashAttribute("message", "コース情報を保存しました。");
        return "redirect:/menus";
    }
}
