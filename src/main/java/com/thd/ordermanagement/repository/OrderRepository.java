package com.thd.ordermanagement.repository;

import com.thd.ordermanagement.model.Order;
import com.thd.ordermanagement.model.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

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
            String email, LocalDateTime endDate, Pageable pageable);

    boolean existsByCustomerEmail(String email);

    long countByOrderStatus(OrderStatus status);
}

