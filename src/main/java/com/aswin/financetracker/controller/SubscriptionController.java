package com.aswin.financetracker.controller;

import com.aswin.financetracker.dto.SubscriptionRequestDto;
import com.aswin.financetracker.model.Subscription;
import com.aswin.financetracker.model.enums.BillingCycle;
import com.aswin.financetracker.model.enums.ExpenseCategory;
import com.aswin.financetracker.service.SubscriptionService;
import jakarta.validation.Valid;
import java.time.LocalDate;
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
@RequestMapping("/subscriptions")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    public SubscriptionController(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @GetMapping
    public String listSubscriptions(Model model) {
        List<Subscription> subscriptions = subscriptionService.getAllSubscriptions();
        List<Subscription> dueWithin3Days = subscriptionService.getSubscriptionsDueWithinDays(3);

        model.addAttribute("subscriptions", subscriptions);
        model.addAttribute("dueWithin3Days", dueWithin3Days);
        model.addAttribute("billingCycles", BillingCycle.values());
        model.addAttribute("categories", ExpenseCategory.values());
        model.addAttribute("subscriptionForm", new SubscriptionRequestDto());
        model.addAttribute("activeTab", "subscriptions");
        return "subscriptions/list";
    }

    @PostMapping
    public String createSubscription(@Valid @ModelAttribute("subscriptionForm") SubscriptionRequestDto dto,
                                     BindingResult result,
                                     RedirectAttributes redirectAttributes,
                                     Model model) {
        if (result.hasErrors()) {
            model.addAttribute("subscriptions", subscriptionService.getAllSubscriptions());
            model.addAttribute("dueWithin3Days", subscriptionService.getSubscriptionsDueWithinDays(3));
            model.addAttribute("billingCycles", BillingCycle.values());
            model.addAttribute("categories", ExpenseCategory.values());
            model.addAttribute("activeTab", "subscriptions");
            return "subscriptions/list";
        }

        subscriptionService.createSubscription(dto);
        redirectAttributes.addFlashAttribute("successMessage", "Subscription '" + dto.getName() + "' created successfully!");
        return "redirect:/subscriptions";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Subscription subscription = subscriptionService.getSubscriptionById(id);
        SubscriptionRequestDto dto = new SubscriptionRequestDto(
            subscription.getId(),
            subscription.getName(),
            subscription.getAmount(),
            subscription.getBillingCycle(),
            subscription.getNextDueDate(),
            subscription.getCategory(),
            subscription.isActive()
        );
        model.addAttribute("subscriptionForm", dto);
        model.addAttribute("billingCycles", BillingCycle.values());
        model.addAttribute("categories", ExpenseCategory.values());
        model.addAttribute("activeTab", "subscriptions");
        return "subscriptions/edit";
    }

    @PostMapping("/edit/{id}")
    public String updateSubscription(@PathVariable Long id,
                                     @Valid @ModelAttribute("subscriptionForm") SubscriptionRequestDto dto,
                                     BindingResult result,
                                     RedirectAttributes redirectAttributes,
                                     Model model) {
        if (result.hasErrors()) {
            dto.setId(id);
            model.addAttribute("billingCycles", BillingCycle.values());
            model.addAttribute("categories", ExpenseCategory.values());
            model.addAttribute("activeTab", "subscriptions");
            return "subscriptions/edit";
        }

        subscriptionService.updateSubscription(id, dto);
        redirectAttributes.addFlashAttribute("successMessage", "Subscription updated successfully!");
        return "redirect:/subscriptions";
    }

    @PostMapping({"/pay/{id}", "/renew/{id}"})
    public String markAsPaid(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        LocalDate prevDueDate = subscriptionService.getSubscriptionById(id).getNextDueDate();
        Subscription sub = subscriptionService.markAsPaid(id);
        if (sub.getNextDueDate() != null && sub.getNextDueDate().equals(prevDueDate)) {
            redirectAttributes.addFlashAttribute("warningMessage",
                "Subscription '" + sub.getName() + "' has already been marked as paid for this billing cycle.");
        } else {
            redirectAttributes.addFlashAttribute("successMessage",
                "Payment of ₹" + sub.getAmount() + " recorded as an Expense! Next due date for '" + sub.getName() + "' is now " + sub.getNextDueDate() + ".");
        }
        return "redirect:/subscriptions";
    }

    @PostMapping("/toggle/{id}")
    public String toggleSubscription(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Subscription sub = subscriptionService.toggleActive(id);
        redirectAttributes.addFlashAttribute("successMessage", "Subscription '" + sub.getName() + "' is now " + (sub.isActive() ? "Active" : "Paused") + ".");
        return "redirect:/subscriptions";
    }

    @PostMapping("/delete/{id}")
    public String deleteSubscription(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        subscriptionService.deleteSubscription(id);
        redirectAttributes.addFlashAttribute("successMessage", "Subscription deleted successfully.");
        return "redirect:/subscriptions";
    }
}
