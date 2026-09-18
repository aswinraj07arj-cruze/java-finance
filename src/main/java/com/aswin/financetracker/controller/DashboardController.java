package com.aswin.financetracker.controller;

import com.aswin.financetracker.dto.BudgetStatusDto;
import com.aswin.financetracker.dto.DashboardSummaryDto;
import com.aswin.financetracker.model.Subscription;
import com.aswin.financetracker.service.BudgetService;
import com.aswin.financetracker.service.DashboardService;
import com.aswin.financetracker.service.SubscriptionService;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    private final DashboardService dashboardService;
    private final SubscriptionService subscriptionService;
    private final BudgetService budgetService;

    public DashboardController(DashboardService dashboardService,
                               SubscriptionService subscriptionService,
                               BudgetService budgetService) {
        this.dashboardService = dashboardService;
        this.subscriptionService = subscriptionService;
        this.budgetService = budgetService;
    }

    @GetMapping({"/", "/dashboard"})
    public String viewDashboard(Model model) {
        DashboardSummaryDto summary = dashboardService.getDashboardSummary();
        List<Subscription> dueSubscriptions = subscriptionService.getSubscriptionsDueWithinDays(3);
        List<BudgetStatusDto> budgets = budgetService.getBudgetsWithCurrentMonthStatus();

        model.addAttribute("summary", summary);
        model.addAttribute("dueSubscriptions", dueSubscriptions);
        model.addAttribute("budgets", budgets);
        model.addAttribute("activeTab", "dashboard");

        return "dashboard/index";
    }
}
