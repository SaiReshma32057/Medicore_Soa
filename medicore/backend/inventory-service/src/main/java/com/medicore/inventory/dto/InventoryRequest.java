package com.medicore.inventory.dto; import jakarta.validation.constraints.Min; public record InventoryRequest(@Min(0) int quantity){}
