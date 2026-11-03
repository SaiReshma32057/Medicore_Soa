package com.medicore.payment.dto; import jakarta.validation.constraints.*; import java.math.BigDecimal; public record PaymentRequest(@NotNull Long orderId,@NotNull @Positive BigDecimal amount){}
