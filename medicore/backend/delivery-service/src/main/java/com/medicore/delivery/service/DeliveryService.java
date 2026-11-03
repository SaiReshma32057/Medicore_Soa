package com.medicore.delivery.service;

import com.medicore.delivery.dto.*;
import com.medicore.delivery.entity.Delivery;
import com.medicore.delivery.repository.DeliveryRepository;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class DeliveryService {
  private final DeliveryRepository repository;

  public DeliveryService(DeliveryRepository repository) {
    this.repository = repository;
  }

  public DeliveryResponse create(DeliveryRequest r) {
    return response(repository.save(new Delivery(r.orderId(), r.address())));
  }

  public DeliveryResponse find(Long id) {
    return response(
        repository
            .findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Delivery not found")));
  }

  public DeliveryResponse status(Long id, String s) {
    if (!Set.of("CREATED", "IN_TRANSIT", "DELIVERED", "FAILED").contains(s))
      throw new IllegalArgumentException("Invalid status");
    Delivery d =
        repository
            .findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Delivery not found"));
    d.setStatus(s);
    return response(repository.save(d));
  }

  private DeliveryResponse response(Delivery d) {
    return new DeliveryResponse(d.getId(), d.getOrderId(), d.getAddress(), d.getStatus());
  }
}
