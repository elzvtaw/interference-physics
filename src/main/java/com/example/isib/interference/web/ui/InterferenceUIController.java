package com.example.isib.interference.web.ui;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class InterferenceUIController {

    @GetMapping("/Interference")
    public String showPage(Model model) {
        model.addAttribute("wavelength", 550.0);
        model.addAttribute("distance", 1.2);
        model.addAttribute("slitWidth", 0.3);
        model.addAttribute("slitNumber", 2);
        model.addAttribute("sourceAngle", 90.0);
        return "interference/Interference";
    }
}