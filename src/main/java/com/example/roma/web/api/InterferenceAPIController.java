package com.example.roma.web.api;

import com.example.roma.model.InterferenceModel;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class InterferenceAPIController {

    private final InterferenceModel interferenceModel = new InterferenceModel();

    @PostMapping("/calculate")
    public Map<String, Object> calculate(
            @RequestParam double wavelength,
            @RequestParam double distance,
            @RequestParam double slitWidth,
            @RequestParam int slitNumber,
            @RequestParam double sourceAngle) {

        Map<String, Object> response = new HashMap<>();

        try {
            // Добавляем входные параметры
            response.put("wavelength", wavelength);
            response.put("distance", distance);
            response.put("slitWidth", slitWidth);
            response.put("slitNumber", slitNumber);
            response.put("sourceAngle", sourceAngle);

            // Вычисления
            InterferenceModel.CalculationResult result =
                    interferenceModel.calculateAll(wavelength, slitWidth, sourceAngle, slitNumber);

            // Добавляем результаты
            response.put("resultIntensity", String.format("%.2f", result.getIntensity()));
            response.put("resultType", result.getType());
            response.put("pathDifference", String.format("%.3f", result.getPathDifference()));
            response.put("sinTheta", String.format("%.4f", result.getSinTheta()));
            response.put("phaseDiff", String.format("%.2f", result.getPhaseDiff()));
            response.put("order", String.format("%.3f", result.getOrder()));
            response.put("maxWavelength", String.format("%.1f", result.getMaxWavelength()));
            response.put("maxOrder", result.getMaxOrder());
            response.put("minWavelength", String.format("%.1f", result.getMinWavelength()));
            response.put("minOrder", result.getMinOrder());

            // Добавляем входные параметры для canvas
            response.put("wavelength", wavelength);
            response.put("distance", distance);
            response.put("slitWidth", slitWidth);
            response.put("slitNumber", slitNumber);
            response.put("sourceAngle", sourceAngle);

        } catch (Exception e) {
            response.put("error", "Ошибка: " + e.getMessage());
        }

        return response;
    }
}