package com.aswin.financetracker.controller;

import com.aswin.financetracker.dto.IncomeRequestDto;
import com.aswin.financetracker.model.Income;
import com.aswin.financetracker.model.enums.IncomeSource;
import com.aswin.financetracker.service.IncomeService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping({"/income", "/incomes"})
public class IncomeController {

    private final IncomeService incomeService;

    public IncomeController(IncomeService incomeService) {
        this.incomeService = incomeService;
    }

    @GetMapping
    public String listIncomes(
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
        @RequestParam(required = false) IncomeSource source,
        Model model
    ) {
        List<Income> incomes = incomeService.getIncomes(startDate, endDate, source);
        model.addAttribute("incomes", incomes);
        model.addAttribute("sources", IncomeSource.values());
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("selectedSource", source);
        model.addAttribute("incomeForm", new IncomeRequestDto());
        model.addAttribute("activeTab", "income");
        return "income/list";
    }

    @PostMapping
    public String createIncome(@Valid @ModelAttribute("incomeForm") IncomeRequestDto dto,
                               BindingResult result,
                               RedirectAttributes redirectAttributes,
                               Model model) {
        if (result.hasErrors()) {
            model.addAttribute("incomes", incomeService.getIncomes(null, null, null));
            model.addAttribute("sources", IncomeSource.values());
            model.addAttribute("activeTab", "income");
            return "income/list";
        }

        incomeService.createIncome(dto);
        redirectAttributes.addFlashAttribute("successMessage", "Income of ₹" + dto.getAmount() + " added successfully!");
        return "redirect:/income";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Income income = incomeService.getIncomeById(id);
        IncomeRequestDto dto = new IncomeRequestDto(
            income.getId(),
            income.getAmount(),
            income.getSource(),
            income.getDate(),
            income.getNote()
        );
        model.addAttribute("incomeForm", dto);
        model.addAttribute("sources", IncomeSource.values());
        model.addAttribute("activeTab", "income");
        return "income/edit";
    }

    @PostMapping("/edit/{id}")
    public String updateIncome(@PathVariable Long id,
                               @Valid @ModelAttribute("incomeForm") IncomeRequestDto dto,
                               BindingResult result,
                               RedirectAttributes redirectAttributes,
                               Model model) {
        if (result.hasErrors()) {
            dto.setId(id);
            model.addAttribute("sources", IncomeSource.values());
            model.addAttribute("activeTab", "income");
            return "income/edit";
        }

        incomeService.updateIncome(id, dto);
        redirectAttributes.addFlashAttribute("successMessage", "Income updated successfully!");
        return "redirect:/income";
    }

    @PostMapping("/delete/{id}")
    public String deleteIncome(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        incomeService.deleteIncome(id);
        redirectAttributes.addFlashAttribute("successMessage", "Income deleted successfully.");
        return "redirect:/income";
    }
}
