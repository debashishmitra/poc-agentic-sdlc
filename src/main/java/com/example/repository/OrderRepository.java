```java
package com.example.orderservice.repository;

import com.example.orderservice.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA Repository for Order entity.
 * Provides CRUD operations and custom query methods for Order management.
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * Find all orders for a specific customer by email.
     * Uses database index on customerEmail for optimal performance.
     *
     * @param customerEmail the customer's email address
     * @return list of orders for the customer
     */
    List<Order> findByCustomerEmailOrderByOrderDateDesc(String customerEmail);

    /**
     * Find orders for a specific customer with pagination.
     *
     * @param customerEmail the customer's email address
     * @param pageable pagination information
     * @return page of orders for the customer
     */
    Page<Order> findByCustomerEmail(String customerEmail, Pageable pageable);

    /**
     * Find orders by status.
     * Uses database index on status for optimal performance.
     *
     * @param status the order status
     * @return list of orders with the specified status
     */
    List<Order> findByStatusOrderByOrderDateDesc(String status);

    /**
     * Find orders by status with pagination.
     *
     * @param status the order status
     * @param pageable pagination information
     * @return page of orders with the specified status
     */
    Page<Order> findByStatus(String status, Pageable pageable);

    /**
     * Find orders for a specific customer with a specific status.
     * Uses composite index on customerEmail and status for optimal performance.
     *
     * @param customerEmail the customer's email address
     * @param status the order status
     * @return list of orders matching the criteria
     */
    List<Order> findByCustomerEmailAndStatusOrderByOrderDateDesc(String customerEmail, String status);

    /**
     * Find orders within a date range.
     *
     * @param startDate the start date (inclusive)
     * @param endDate the end date (inclusive)
     * @return list of orders within the date range
     */
    List<Order> findByOrderDateBetweenOrderByOrderDateDesc(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Find orders within a date range with pagination.
     *
     * @param startDate the start date (inclusive)
     * @param endDate the end date (inclusive)
     * @param pageable pagination information
     * @return page of orders within the date range
     */
    Page<Order> findByOrderDateBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    /**
     * Find orders for a customer within a date range.
     * Uses composite index on customerEmail and orderDate for optimal performance.
     *
     * @param customerEmail the customer's email address
     * @param startDate the start date (inclusive)
     * @param endDate the end date (inclusive)
     * @return list of orders matching the criteria
     */
    List<Order> findByCustomerEmailAndOrderDateBetweenOrderByOrderDateDesc(
            String customerEmail, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Find orders with total amount greater than or equal to specified amount.
     *
     * @param minAmount the minimum total amount
     * @return list of orders with total amount >= minAmount
     */
    List<Order> findByTotalAmountGreaterThanEqualOrderByTotalAmountDesc(BigDecimal minAmount);

    /**
     * Find orders with total amount between specified range.
     *
     * @param minAmount the minimum total amount (inclusive)
     * @param maxAmount the maximum total amount (inclusive)
     * @return list of orders within the amount range
     */
    List<Order> findByTotalAmountBetweenOrderByOrderDateDesc(BigDecimal minAmount, BigDecimal maxAmount);

    /**
     * Count orders by status.
     *
     * @param status the order status
     * @return number of orders with the specified status
     */
    long countByStatus(String status);

    /**
     * Count orders for a specific customer.
     *
     * @param customerEmail the customer's email address
     * @return number of orders for the customer
     */
    long countByCustomerEmail(String customerEmail);

    /**
     * Check if an order exists for a customer with a specific status.
     *
     * @param customerEmail the customer's email address
     * @param status the order status
     * @return true if such order exists, false otherwise
     */
    boolean existsByCustomerEmailAndStatus(String customerEmail, String status);

    /**
     * Find the most recent order for a customer.
     *
     * @param customerEmail the customer's email address
     * @return optional containing the most recent order, or empty if no orders exist
     */
    Optional<Order> findFirstByCustomerEmailOrderByOrderDateDesc(String customerEmail);

    /**
     * Find orders by multiple statuses.
     *
     * @param statuses list of order statuses
     * @return list of orders with any of the specified statuses
     */
    List<Order> findByStatusInOrderByOrderDateDesc(List<String> statuses);

    /**
     * Find orders by multiple statuses with pagination.
     *
     * @param statuses list of order statuses
     * @param pageable pagination information
     * @return page of orders with any of the specified statuses
     */
    Page<Order> findByStatusIn(List<String> statuses, Pageable pageable);

    /**
     * Custom query to find orders summary by customer.
     * Returns customer email, total orders count, and total amount.
     *
     * @return list of object arrays containing [customerEmail, orderCount, totalAmount]
     */
    @Query("SELECT o.customerEmail, COUNT(o), SUM(o.totalAmount) " +
           "FROM Order o " +
           "GROUP BY o.customerEmail " +
           "ORDER BY SUM(o.totalAmount) DESC")
    List<Object[]> findOrderSummaryByCustomer();

    /**
     * Custom query to find orders summary by status.
     * Returns status, count of orders, and total amount for each status.
     *
     * @return list of object arrays containing [status, orderCount, totalAmount]
     */
    @Query("SELECT o.status, COUNT(o), SUM(o.totalAmount) " +
           "FROM Order o " +
           "GROUP BY o.status " +
           "ORDER BY COUNT(o) DESC")
    List<Object[]> findOrderSummaryByStatus();

    /**
     * Custom query to find top customers by total order value.
     *
     * @param limit maximum number of customers to return
     * @return list of object arrays containing [customerEmail, totalAmount, orderCount]
     */
    @Query(value = "SELECT customer_email, SUM(total_amount) as total_spent, COUNT(*) as order_count " +
                   "FROM orders " +
                   "GROUP BY customer_email " +
                   "ORDER BY total_spent DESC " +
                   "LIMIT :limit", nativeQuery = true)
    List<Object[]> findTopCustomersByTotalValue(@Param("limit") int limit);

    /**
     * Custom query to find orders that need attention (older than specified days in pending status).
     *
     * @param days number of days to look back
     * @param status the status to filter by (typically 'PENDING' or 'PROCESSING')
     * @return list of orders that need attention
     */
    @Query("SELECT o FROM Order o " +
           "WHERE o.status = :status " +
           "AND o.orderDate < :cutoff