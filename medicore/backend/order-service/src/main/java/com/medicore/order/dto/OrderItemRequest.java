package com.medicore.order.dto; import jakarta.validation.constraints.*; public record OrderItemRequest(@NotNull Long medicineId,@Min(1) int quantity){}
