package com.medicore.medicine.dto;

import java.math.BigDecimal;
public record MedicineResponse(Long id, String name, BigDecimal price, boolean active) { }
