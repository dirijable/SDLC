package com.dirijable.bpproject.model;

import java.time.LocalDate;

public record BloodPressureInput(
        LocalDate birthDate,
        double weightKg,
        int systolic,
        int diastolic
) {
}
