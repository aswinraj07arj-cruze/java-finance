package com.aswin.financetracker.controller;

import com.aswin.financetracker.dto.BudgetRequestDto;
import com.aswin.financetracker.dto.BudgetStatusDto;
import com.aswin.financetracker.model.enums.ExpenseCategory;
import com.aswin.financetracker.service.BudgetService;
import jakarta.validation.Valid;
import java.util.List;
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
@RequestMapping("/budgets")
public class BudgetController {

    private final BudgetService budgetService;

    public BudgetController(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    @GetMapping
    public String listBudgets(Model model) {
        List<BudgetStatusDto> budgets = budgetService.getBudgetsWithCurrentMonthStatus();
        model.addAttribute("budgets", budgets);
        model.addAttribute("categories", ExpenseCategory.values());
        model.addAttribute("budgetForm", new BudgetRequestDto());
        model.addAttribute("activeTab", "budgets");
        return "budgets/list";
    }

    @PostMapping
    public String saveOrUpdateBudget(@Valid @ModelAttribute("budgetForm") BudgetRequestDto dto,
                                     BindingResult result,
                                     RedirectAttributes redirectAttributes,
                                     Model model) {
        if (result.hasErrors()) {
            model.addAttribute("budgets", budgetService.getBudgetsWithCurrentMonthStatus());
            model.addAttribute("categories", ExpenseCategory.values());
            model.addAttribute("activeTab", "budgets");
            return "budgets/list";
        }

        budgetService.saveOrUpdateBudget(dto);
        redirectAttributes.addFlashAttribute("successMessage", "Monthly budget for " + dto.getCategory().getDisplayName() + " saved successfully!");
        return "redirect:/budgets";
    }

    @PostMapping("/delete/{id}")
    public String deleteBudget(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        budgetService.deleteBudget(id);
        redirectAttributes.addFlashAttribute("successMessage", "Budget deleted successfully.");
        return "redirect:/budgets";
    }
}
