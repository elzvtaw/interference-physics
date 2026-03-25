package com.example.roma.web;

import com.example.roma.model.InterferenceModel;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class RomanController {

    private final InterferenceModel interferenceModel = new InterferenceModel();

    @GetMapping("/page")
    public String showPage(Model model) {
        // Только базовые атрибуты
        model.addAttribute("wavelength", 550.0);
        model.addAttribute("distance", 1.2);
        model.addAttribute("slitWidth", 0.3);
        model.addAttribute("slitNumber", 2);
        model.addAttribute("sourceAngle", 90.0);
        model.addAttribute("resultIntensity", "4.00");
        model.addAttribute("resultType", "МАКСИМУМ (m = 1)");
        model.addAttribute("sinTheta", "1.0000");
        model.addAttribute("pathDifference", "0.300");
        model.addAttribute("order", "1.000");
        model.addAttribute("phaseDiff", "3.14");
        model.addAttribute("maxWavelength", "550.0");
        model.addAttribute("maxOrder", 1);
        model.addAttribute("minWavelength", "366.7");
        model.addAttribute("minOrder", 1);

        return "page";
    }

    @GetMapping("/")
    public String redirectToPage() {
        return "redirect:/page";
    }

    @PostMapping("/calculate")
    public String calculate(
            @RequestParam(value = "wavelength", defaultValue = "550") double wavelength,
            @RequestParam(value = "distance", defaultValue = "1.2") double distance,
            @RequestParam(value = "slitWidth", defaultValue = "0.3") double slitWidth,
            @RequestParam(value = "slitNumber", defaultValue = "2") int slitNumber,
            @RequestParam(value = "sourceAngle", defaultValue = "90") double sourceAngle,
            Model model) {

        try {
            // Добавляем входные параметры
            model.addAttribute("wavelength", wavelength);
            model.addAttribute("distance", distance);
            model.addAttribute("slitWidth", slitWidth);
            model.addAttribute("slitNumber", slitNumber);
            model.addAttribute("sourceAngle", sourceAngle);

            // Вычисления
            InterferenceModel.CalculationResult result =
                    interferenceModel.calculateAll(wavelength, slitWidth, sourceAngle, slitNumber);

            // Добавляем результаты
            model.addAttribute("resultIntensity", String.format("%.2f", result.getIntensity()));
            model.addAttribute("resultType", result.getType());
            model.addAttribute("pathDifference", String.format("%.3f", result.getPathDifference()));
            model.addAttribute("sinTheta", String.format("%.4f", result.getSinTheta()));
            model.addAttribute("phaseDiff", String.format("%.2f", result.getPhaseDiff()));
            model.addAttribute("order", String.format("%.3f", result.getOrder()));
            model.addAttribute("maxWavelength", String.format("%.1f", result.getMaxWavelength()));
            model.addAttribute("maxOrder", result.getMaxOrder());
            model.addAttribute("minWavelength", String.format("%.1f", result.getMinWavelength()));
            model.addAttribute("minOrder", result.getMinOrder());

        } catch (Exception e) {
            model.addAttribute("error", "Ошибка: " + e.getMessage());
        }

        return "page";
    }
}