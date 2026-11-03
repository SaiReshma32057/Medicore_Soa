package com.medicore.delivery.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

import com.medicore.delivery.repository.DeliveryRepository;
import org.junit.jupiter.api.Test;

class DeliveryServiceTest {
  @Test
  void serviceConstructs() {
    assertNotNull(new DeliveryService(mock(DeliveryRepository.class)));
  }
}
