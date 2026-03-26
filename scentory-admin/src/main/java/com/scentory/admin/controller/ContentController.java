package com.scentory.admin.controller;

import com.scentory.admin.entity.FaqEntry;
import com.scentory.admin.entity.Notice;
import com.scentory.admin.repository.FaqEntryRepository;
import com.scentory.admin.repository.NoticeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/contents")
@RequiredArgsConstructor
public class ContentController {

    private final NoticeRepository noticeRepository;
    private final FaqEntryRepository faqEntryRepository;

    @GetMapping
    public String index(Model model) {
        model.addAttribute("pageTitle", "コンテンツ管理");
        model.addAttribute("notices", noticeRepository.findAll());
        model.addAttribute("faqs", faqEntryRepository.findAll());
        return "contents/index";
    }

    @GetMapping("/notices/new")
    public String newNotice(Model model) {
        model.addAttribute("pageTitle", "お知らせ登録");
        model.addAttribute("notice", new Notice());
        return "contents/notice-form";
    }

    @GetMapping("/notices/{id}/edit")
    public String editNotice(@PathVariable Long id, Model model) {
        model.addAttribute("pageTitle", "お知らせ編集");
        model.addAttribute("notice", noticeRepository.findById(id).orElseThrow());
        return "contents/notice-form";
    }

    @PostMapping("/notices/save")
    public String saveNotice(@ModelAttribute Notice notice, RedirectAttributes redirectAttributes) {
        noticeRepository.save(notice);
        redirectAttributes.addFlashAttribute("message", "お知らせを保存しました。");
        return "redirect:/contents";
    }

    @GetMapping("/faqs/new")
    public String newFaq(Model model) {
        model.addAttribute("pageTitle", "FAQ登録");
        model.addAttribute("faq", new FaqEntry());
        return "contents/faq-form";
    }

    @GetMapping("/faqs/{id}/edit")
    public String editFaq(@PathVariable Long id, Model model) {
        model.addAttribute("pageTitle", "FAQ編集");
        model.addAttribute("faq", faqEntryRepository.findById(id).orElseThrow());
        return "contents/faq-form";
    }

    @PostMapping("/faqs/save")
    public String saveFaq(@ModelAttribute("faq") FaqEntry faq, RedirectAttributes redirectAttributes) {
        faqEntryRepository.save(faq);
        redirectAttributes.addFlashAttribute("message", "FAQを保存しました。");
        return "redirect:/contents";
    }

    @PostMapping("/banner")
    public String updateBanner(@RequestParam String bannerMessage, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("message", "LP/バナー管理は次フェーズ対応です。仮メッセージ: " + bannerMessage);
        return "redirect:/contents";
    }
}
