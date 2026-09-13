package com.dirijable.bpproject.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.time.LocalDate;
import java.time.Period;

public class BloodPressureModel {

    public static final String PROP_DATA_CHANGED = "dataChanged";

    //объект который внутри сбея хранит подписчиков и опеваещет иъ
    private final PropertyChangeSupport support = new PropertyChangeSupport(this);

    // последние введённые данные (запоминаем для восстановления в диалоге)
    private LocalDate birthDate;
    private double weightKg;
    private int systolic;   // верхнее давление
    private int diastolic;  // нижнее давление

    private boolean hasData = false;

    // результат последнего расчёта
    private double idealSystolic;
    private double idealDiastolic;
    private String estimation = "";

    //метод для подписки на изменения из мейнфрейм
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    //для изменеия данных снаружи модели
    public void updateData(LocalDate birthDate, double weightKg, int systolic, int diastolic) {
        validate(birthDate, weightKg, systolic, diastolic);

        this.birthDate = birthDate;
        this.weightKg = weightKg;
        this.systolic = systolic;
        this.diastolic = diastolic;
        this.hasData = true;//нужно для getLastInput

        recalculate();

        //прин. тип события, старое знач. и новое, внутри которой создается propertyChangeEvent с этими данными и оповещает
        //подписчиков
        support.firePropertyChange(PROP_DATA_CHANGED, null, this);
    }

    private void validate(LocalDate birthDate, double weightKg, int systolic, int diastolic) {
        if (birthDate == null) {
            throw new IllegalArgumentException("Не указана дата рождения");
        }
        if (birthDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Дата рождения не может быть в будущем");
        }
        if (Period.between(birthDate, LocalDate.now()).getYears() > 130) {
            throw new IllegalArgumentException("Некорректная дата рождения (возраст > 130 лет)");
        }
        if (!Double.isFinite(weightKg) || weightKg <= 0 || weightKg > 400) {
            throw new IllegalArgumentException("Некорректный вес (0 < вес <= 400 кг)");
        }
        if (systolic < 60 || systolic > 260) {
            throw new IllegalArgumentException("Некорректное верхнее давление (60..260)");
        }
        if (diastolic < 30 || diastolic > 160) {
            throw new IllegalArgumentException("Некорректное нижнее давление (30..160)");
        }
        if (systolic <= diastolic) {
            throw new IllegalArgumentException("Верхнее давление должно быть больше нижнего");
        }
    }

    private void recalculate() {
        int age = Period.between(birthDate, LocalDate.now()).getYears();

        // Формула для идеального давления
        idealSystolic = 109 + 0.5 * age + 0.1 * weightKg;
        idealDiastolic = 63 + 0.1 * age + 0.15 * weightKg;

        double tolerance = 0.10; // допуск 10% от идеального значения

        boolean systolicHigh = systolic > idealSystolic * (1 + tolerance);
        boolean systolicLow = systolic < idealSystolic * (1 - tolerance);
        boolean diastolicHigh = diastolic > idealDiastolic * (1 + tolerance);
        boolean diastolicLow = diastolic < idealDiastolic * (1 - tolerance);

        if (systolicHigh || diastolicHigh) {
            estimation = "Повышенное давление";
        } else if (systolicLow || diastolicLow) {
            estimation = "Пониженное давление";
        } else {
            estimation = "Норма";
        }
    }

    //нужне для контроллера, так как он вызывает его при открытии диалога
    public BloodPressureInput getLastInput() {
        if (!hasData) { //если данные не вводились ни разу, то вернем null
            return null;
        }
        return new BloodPressureInput(birthDate, weightKg, systolic, diastolic);
    }

    public double getIdealSystolic() {
        return idealSystolic;
    }

    public double getIdealDiastolic() {
        return idealDiastolic;
    }

    public String getEstimation() {
        return estimation;
    }
}
