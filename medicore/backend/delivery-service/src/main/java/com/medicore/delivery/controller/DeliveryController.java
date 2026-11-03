package com.medicore.delivery.controller;

import com.medicore.delivery.dto.*;
import com.medicore.delivery.service.DeliveryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/deliveries")
public class DeliveryController {
  private final DeliveryService service;

  public DeliveryController(DeliveryService service) {
    this.service = service;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public DeliveryResponse create(@Valid @RequestBody DeliveryRequest r) {
    return service.create(r);
  }

  @GetMapping("/{id}")
  public DeliveryResponse find(@PathVariable Long id) {
    return service.find(id);
  }

  @PatchMapping("/{id}/status")
  public DeliveryResponse status(@PathVariable Long id, @RequestParam String value) {
    return service.status(id, value);
  }
}
