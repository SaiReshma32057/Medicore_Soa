package com.medicore.order.service;

import com.medicore.order.client.InventoryClient;
import com.medicore.order.dto.*;
import com.medicore.order.repository.OrderRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class OrderServiceTest {
    @Test
    void inventoryFailurePreventsSave() {
        InventoryClient client = mock(InventoryClient.class);
        when(client.reserve(anyLong(), any())).thenThrow(new RuntimeException("downstream"));
        OrderRepository repository = mock(OrderRepository.class);

        assertThrows(IllegalStateException.class, () -> new OrderService(repository, client)
            .create(new OrderRequest(1L, List.of(new OrderItemRequest(2L, 1)))));
        verifyNoInteractions(repository);
    }

    @Test
    void laterReservationFailureReleasesEarlierReservation() {
        InventoryClient client = mock(InventoryClient.class);
        when(client.reserve(eq(1L), any())).thenReturn(new InventoryReservationResponse(1L, 3, true));
        when(client.reserve(eq(2L), any())).thenThrow(new RuntimeException("downstream"));
        OrderRepository repository = mock(OrderRepository.class);

        assertThrows(IllegalStateException.class, () -> new OrderService(repository, client)
            .create(new OrderRequest(1L, List.of(
                new OrderItemRequest(1L, 2), new OrderItemRequest(2L, 1)))));
        verify(client).release(eq(1L), any());
        verifyNoInteractions(repository);
    }
}
