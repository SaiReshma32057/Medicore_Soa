package com.medicore.medicine.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record MedicineRequest(@NotBlank String name, @NotNull @Positive BigDecimal price, boolean active) { }
