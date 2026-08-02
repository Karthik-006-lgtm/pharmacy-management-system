package com.pharmacy.exception;

import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleResourceNotFound(ResourceNotFoundException ex, Model model) {
        model.addAttribute("error", ex.getMessage());
        return "error/404";
    }
    
    @ExceptionHandler(DuplicateResourceException.class)
    public String handleDuplicateResource(DuplicateResourceException ex, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error", ex.getMessage());
        return "redirect:/";
    }
    
    @ExceptionHandler(InsufficientStockException.class)
    public String handleInsufficientStock(InsufficientStockException ex, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error", ex.getMessage());
        return "redirect:/medicines";
    }
    
    @ExceptionHandler(FileUploadException.class)
    public String handleFileUpload(FileUploadException ex, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error", ex.getMessage());
        return "redirect:/orders/checkout";
    }
    
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleGenericException(Exception ex, Model model) {
        ex.printStackTrace(); // Log the full stack trace
        System.err.println("ERROR: " + ex.getMessage());
        model.addAttribute("error", "An unexpected error occurred. Please try again.");
        return "error/500";
    }
}
