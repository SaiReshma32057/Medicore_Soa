package com.medicore.delivery.dto; import jakarta.validation.constraints.*; public record DeliveryRequest(@NotNull Long orderId,@NotBlank String address){}
