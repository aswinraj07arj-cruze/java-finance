package com.aswin.financetracker.controller;

import jakarta.servlet.http.HttpServletRequest;
import java.util.NoSuchElementException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NoSuchElementException.class)
    public String handleNotFound(NoSuchElementException ex, HttpServletRequest request, RedirectAttributes redirectAttributes, Model model) {
        // If it's an AJAX or API request, return a simple error or let it forward
        if (request.getRequestURI().startsWith("/api/")) {
            model.addAttribute("errorMessage", ex.getMessage());
            return "error/404";
        }
        redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        return "redirect:/";
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public String handleBadRequest(IllegalArgumentException ex, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        return "redirect:/";
    }

    @ExceptionHandler(Exception.class)
    public String handleGeneralException(Exception ex, Model model) {
        model.addAttribute("errorMessage", "An unexpected error occurred. Please try again or contact support.");
        model.addAttribute("errorDetails", ex.getClass().getSimpleName() + ": " + ex.getMessage());
        return "error/500";
    }
}
