package com.thd.ordermanagement.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.thd.ordermanagement.model.Order;
import com.thd.ordermanagement.model.OrderStatus;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByOrderStatus(OrderStatus status);

    List<Order> findByCustomerEmail(String email);

    Page<Order> findByCustomerEmail(String email, Pageable pageable);

    List<Order> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    Page<Order> findByCustomerEmailAndCreatedAtBetween(
            String email, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    Page<Order> findByCustomerEmailAndCreatedAtGreaterThanEqual(
            String email, LocalDateTime startDate, Pageable pageable);

    Page<Order> findByCustomerEmailAndCreatedAtLessThanEqual(
            String email, LocalDateTime toDate, Pageable pageable);

    boolean existsByCustomerEmail(String email);

    long countByOrderStatus(OrderStatus status);

    /**
     * STORY-004: Retrieve the most recent orders sorted by creation date descending.
     * Used by the GET /api/v1/orders/recent endpoint.
     *
     * Usage: orderRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, limit))
     * This returns a List of the top N most recently created orders.
     */
    List<Order> findAllByOrderByCreatedAtDesc(Pageable pageable);
}