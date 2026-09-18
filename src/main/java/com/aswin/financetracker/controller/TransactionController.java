package com.aswin.financetracker.controller;

import com.aswin.financetracker.dto.TransactionFilterDto;
import com.aswin.financetracker.dto.TransactionViewDto;
import com.aswin.financetracker.model.enums.ExpenseCategory;
import com.aswin.financetracker.model.enums.IncomeSource;
import com.aswin.financetracker.service.TransactionService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping
    public String viewTransactions(@ModelAttribute("filter") TransactionFilterDto filter, Model model) {
        Page<TransactionViewDto> page = transactionService.getUnifiedTransactions(filter);

        model.addAttribute("page", page);
        model.addAttribute("transactions", page.getContent());
        model.addAttribute("categories", ExpenseCategory.values());
        model.addAttribute("sources", IncomeSource.values());
        model.addAttribute("filter", filter);
        model.addAttribute("activeTab", "transactions");

        return "transactions/list";
    }
}
