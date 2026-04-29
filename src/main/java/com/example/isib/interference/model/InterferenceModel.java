package com.example.isib.interference.model;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



public class InterferenceModel {

    // Класс для хранения результата расчета
    @Getter
    @Setter
    public static class CalculationResult {
        private double intensity = 4.0;
        private String type = "МАКСИМУМ (m = 1)";
        private double pathDifference = 0.3;
        private double sinTheta = 1.0;
        private double phaseDiff = 3.14;
        private double order = 1.0;
        private boolean isMaximum = true;
        private boolean isMinimum = false;
        private double maxWavelength = 550.0;
        private int maxOrder = 1;
        private double minWavelength = 366.7;
        private int minOrder = 1;
        private List<Map<String, Object>> nearbyExtremums = new ArrayList<>();

    }

    /**
     * Главный метод расчета всех параметров интерференции на основе ВВЕДЕННЫХ данных
     */
    public CalculationResult calculateAll(double wavelength, double slitWidth, double sourceAngle, int slitNumber) {
        CalculationResult result = new CalculationResult();

        try {
            // Защита от деления на ноль
            if (slitNumber <= 0) slitNumber = 2;
            if (wavelength <= 0) wavelength = 550;
            if (slitWidth <= 0) slitWidth = 0.3;

            // Перевод в СИ
            double lambda_m = wavelength * 1e-9;
            double d_m = slitWidth * 1e-3;
            double angleRad = Math.toRadians(sourceAngle);

            // 1. Вычисляем sinθ
            double sinTheta = Math.sin(angleRad);
            result.setSinTheta(sinTheta);

            // 2. Вычисляем разность хода Δ = d·sinθ
            double pathDifference = d_m * sinTheta;
            result.setPathDifference(pathDifference * 1e6);

            // 3. Вычисляем порядок интерференции m = Δ/λ
            double order = pathDifference / lambda_m;
            result.setOrder(order);

            // 4. Вычисляем разность фаз φ = 2π·Δ/λ
            double phaseDiff = (2 * Math.PI * pathDifference) / lambda_m;
            result.setPhaseDiff(phaseDiff);

            // 5. Вычисляем относительную интенсивность
            double intensity = calculateIntensity(slitNumber, phaseDiff);
            result.setIntensity(Math.min(intensity, 100));

            // 6. Определяем тип интерференции
            double epsilon = 0.01;
            double mRounded = Math.round(order);
            double mHalfRounded = Math.round(order - 0.5) + 0.5;

            boolean isMaximum = Math.abs(order - mRounded) < epsilon;
            boolean isMinimum = Math.abs(order - mHalfRounded) < epsilon;

            result.setMaximum(isMaximum);
            result.setMinimum(isMinimum);

            if (isMaximum) {
                result.setType(String.format("МАКСИМУМ (m = %d)", (int) mRounded));
            } else if (isMinimum) {
                result.setType(String.format("МИНИМУМ (m = %d.5)", (int) (mHalfRounded - 0.5)));
            } else {
                result.setType("ПРОМЕЖУТОЧНОЕ ЗНАЧЕНИЕ");
            }

            // 7. Вычисляем длину волны для максимума
            int nearestMaxOrder = (int) Math.round(order);
            if (nearestMaxOrder == 0) nearestMaxOrder = 1;
            double maxWavelength = Math.abs((pathDifference / nearestMaxOrder) * 1e9);
            result.setMaxWavelength(maxWavelength);
            result.setMaxOrder(nearestMaxOrder);

            // 8. Вычисляем длину волны для минимума
            int nearestMinOrder = (int) Math.floor(order);
            double minWavelength = Math.abs((pathDifference / (nearestMinOrder + 0.5)) * 1e9);
            result.setMinWavelength(minWavelength);
            result.setMinOrder(nearestMinOrder);

            // 9. Находим ближайшие экстремумы
            List<Map<String, Object>> extremums = findNearbyExtremums(wavelength, slitWidth, sourceAngle);
            result.setNearbyExtremums(extremums);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    /**
     * Вычисление интенсивности для N щелей
     */
    private double calculateIntensity(int slitNumber, double phaseDiff) {
        if (slitNumber == 2) {
            return 4 * Math.pow(Math.cos(phaseDiff / 2), 2);
        } else {
            if (Math.abs(Math.sin(phaseDiff / 2)) < 1e-10) {
                return slitNumber * slitNumber;
            } else {
                return Math.pow(
                        Math.sin(slitNumber * phaseDiff / 2) / Math.sin(phaseDiff / 2),
                        2
                );
            }
        }
    }

    /**
     * Находит ближайшие максимумы и минимумы для текущих параметров
     */
    private List<Map<String, Object>> findNearbyExtremums(double wavelength, double slitWidth, double sourceAngle) {
        List<Map<String, Object>> extremums = new ArrayList<>();

        try {
            double lambda_m = wavelength * 1e-9;
            double d_m = slitWidth * 1e-3;
            double angleRad = Math.toRadians(sourceAngle);
            double sinTheta = Math.sin(angleRad);
            double pathDifference = d_m * sinTheta;
            double currentM = pathDifference / lambda_m;

            // Ближайшие максимумы
            int mMax1 = (int) Math.floor(currentM);
            int mMax2 = (int) Math.ceil(currentM);

            if (mMax1 >= -10 && mMax1 <= 10 && mMax1 != 0) {
                Map<String, Object> max1 = new HashMap<>();
                max1.put("type", "МАКСИМУМ");
                max1.put("order", mMax1);
                double angle = calculateAngleForOrder(mMax1, wavelength, slitWidth);
                max1.put("angle", Double.isNaN(angle) ? 0.0 : angle);
                max1.put("condition", String.format("d·sinθ = %d·λ", mMax1));
                extremums.add(max1);
            }

            if (mMax2 >= -10 && mMax2 <= 10 && mMax2 != mMax1 && mMax2 != 0) {
                Map<String, Object> max2 = new HashMap<>();
                max2.put("type", "МАКСИМУМ");
                max2.put("order", mMax2);
                double angle = calculateAngleForOrder(mMax2, wavelength, slitWidth);
                max2.put("angle", Double.isNaN(angle) ? 0.0 : angle);
                max2.put("condition", String.format("d·sinθ = %d·λ", mMax2));
                extremums.add(max2);
            }

            // Ближайшие минимумы
            double mMin1 = Math.floor(currentM) + 0.5;
            double mMin2 = Math.ceil(currentM) + 0.5;

            if (mMin1 >= -10 && mMin1 <= 10) {
                Map<String, Object> min1 = new HashMap<>();
                min1.put("type", "МИНИМУМ");
                min1.put("order", mMin1);
                double angle = calculateAngleForOrder(mMin1, wavelength, slitWidth);
                min1.put("angle", Double.isNaN(angle) ? 0.0 : angle);
                min1.put("condition", String.format("d·sinθ = %.1f·λ", mMin1));
                extremums.add(min1);
            }

            if (mMin2 >= -10 && mMin2 <= 10 && Math.abs(mMin2 - mMin1) > 0.1) {
                Map<String, Object> min2 = new HashMap<>();
                min2.put("type", "МИНИМУМ");
                min2.put("order", mMin2);
                double angle = calculateAngleForOrder(mMin2, wavelength, slitWidth);
                min2.put("angle", Double.isNaN(angle) ? 0.0 : angle);
                min2.put("condition", String.format("d·sinθ = %.1f·λ", mMin2));
                extremums.add(min2);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return extremums;
    }

    /**
     * Вычисляет угол для заданного порядка
     */
    private double calculateAngleForOrder(double order, double wavelength, double slitWidth) {
        try {
            double lambda_m = wavelength * 1e-9;
            double d_m = slitWidth * 1e-3;
            double sinTheta = (order * lambda_m) / d_m;

            if (Math.abs(sinTheta) <= 1) {
                return Math.toDegrees(Math.asin(sinTheta));
            }
        } catch (Exception e) {
            // Игнорируем
        }
        return Double.NaN;
    }
}