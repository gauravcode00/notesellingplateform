package com.notesapp.notesellingplateform.service;

import com.notesapp.notesellingplateform.entity.Order;
import com.notesapp.notesellingplateform.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderService {
    private final OrderRepository orderRepo;

    public OrderService(OrderRepository orderRepo) {
        this.orderRepo = orderRepo;
    }

    public Order create(Order order) {
        return orderRepo.save(order);
    }

    public List<Order> findByBuyerId(Long buyerId) {
        return orderRepo.findByBuyerId(buyerId);
    }

    // You’ll add logic here later to handle payment callbacks, update status, etc.
}
