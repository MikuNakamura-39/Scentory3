package com.scentory.admin.controller;

import com.scentory.admin.entity.SystemSetting;
import com.scentory.admin.repository.SystemSettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/settings")
@RequiredArgsConstructor
public class SettingsController {

    private final SystemSettingRepository systemSettingRepository;

    @GetMapping
    public String index(Model model) {
        model.addAttribute("pageTitle", "システム設定");
        model.addAttribute("settings", systemSettingRepository.findAll());
        model.addAttribute("setting", new SystemSetting());
        return "settings/index";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute SystemSetting setting, RedirectAttributes redirectAttributes) {
        systemSettingRepository.save(setting);
        redirectAttributes.addFlashAttribute("message", "設定を保存しました。");
        return "redirect:/settings";
    }
}
