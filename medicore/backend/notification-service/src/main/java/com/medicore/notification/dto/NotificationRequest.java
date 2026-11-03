package com.medicore.notification.dto; import jakarta.validation.constraints.*; public record NotificationRequest(@NotNull Long userId,@NotBlank String message){}
