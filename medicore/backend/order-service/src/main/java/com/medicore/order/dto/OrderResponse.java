package com.medicore.order.dto; import java.util.List; public record OrderResponse(Long id,Long userId,List<OrderItemRequest> items,String status){}
