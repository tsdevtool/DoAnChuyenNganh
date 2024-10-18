package com.example.WebSiteDatLich.controller;

import com.example.WebSiteDatLich.model.Position;
import com.example.WebSiteDatLich.service.PositionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class PositionController {

    @Autowired
    private PositionService positionService;

    // Show list of positions
    @GetMapping("/positions")
    public String getPositions(Model model) throws InterruptedException {
        model.addAttribute("positions", positionService.getAllPositions());
        return "position-list";  // View for displaying list of positions
    }

    // Show form to add a new position
    @GetMapping("/positions/new")
    public String showAddPositionForm(Model model) {
        model.addAttribute("position", new Position());
        return "position-form";  // View for adding a new position
    }

    // Handle form submission for adding a new position
    @PostMapping("/positions")
    public String savePosition(@ModelAttribute("position") Position position) {
        positionService.savePosition(position);
        return "redirect:/positions";  // Redirect to the list after saving
    }
}
