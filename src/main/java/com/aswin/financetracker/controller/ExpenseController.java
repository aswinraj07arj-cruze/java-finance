package com.aswin.financetracker.controller;

import com.aswin.financetracker.dto.ExpenseRequestDto;
import com.aswin.financetracker.model.Expense;
import com.aswin.financetracker.model.enums.ExpenseCategory;
import com.aswin.financetracker.service.ExpenseService;
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
@RequestMapping({"/expense", "/expenses"})
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @GetMapping
    public String listExpenses(
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
        @RequestParam(required = false) ExpenseCategory category,
        Model model
    ) {
        List<Expense> expenses = expenseService.getExpenses(startDate, endDate, category);
        model.addAttribute("expenses", expenses);
        model.addAttribute("categories", ExpenseCategory.values());
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("expenseForm", new ExpenseRequestDto());
        model.addAttribute("activeTab", "expense");
        return "expense/list";
    }

    @PostMapping
    public String createExpense(@Valid @ModelAttribute("expenseForm") ExpenseRequestDto dto,
                                BindingResult result,
                                RedirectAttributes redirectAttributes,
                                Model model) {
        if (result.hasErrors()) {
            model.addAttribute("expenses", expenseService.getExpenses(null, null, null));
            model.addAttribute("categories", ExpenseCategory.values());
            model.addAttribute("activeTab", "expense");
            return "expense/list";
        }

        expenseService.createExpense(dto);
        redirectAttributes.addFlashAttribute("successMessage", "Expense of ₹" + dto.getAmount() + " recorded successfully!");
        return "redirect:/expense";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Expense expense = expenseService.getExpenseById(id);
        ExpenseRequestDto dto = new ExpenseRequestDto(
            expense.getId(),
            expense.getAmount(),
            expense.getCategory(),
            expense.getDate(),
            expense.getDescription()
        );
        model.addAttribute("expenseForm", dto);
        model.addAttribute("categories", ExpenseCategory.values());
        model.addAttribute("activeTab", "expense");
        return "expense/edit";
    }

    @PostMapping("/edit/{id}")
    public String updateExpense(@PathVariable Long id,
                                @Valid @ModelAttribute("expenseForm") ExpenseRequestDto dto,
                                BindingResult result,
                                RedirectAttributes redirectAttributes,
                                Model model) {
        if (result.hasErrors()) {
            dto.setId(id);
            model.addAttribute("categories", ExpenseCategory.values());
            model.addAttribute("activeTab", "expense");
            return "expense/edit";
        }

        expenseService.updateExpense(id, dto);
        redirectAttributes.addFlashAttribute("successMessage", "Expense updated successfully!");
        return "redirect:/expense";
    }

    @PostMapping("/delete/{id}")
    public String deleteExpense(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        expenseService.deleteExpense(id);
        redirectAttributes.addFlashAttribute("successMessage", "Expense deleted successfully.");
        return "redirect:/expense";
    }
}
