package org.example.orderflow.repository;

import org.example.orderflow.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository <OrderItem, Integer> {
}
