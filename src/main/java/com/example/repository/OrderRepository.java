```java
package com.company.orderservice.repository;

import com.company.orderservice.entity.Order;
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
 * Spring Data JPA repository for Order entity operations.
 * Provides CRUD operations and custom query methods for Order management.
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * Find all orders by customer email address.
     * 
     * @param customerEmail the customer's email address
     * @return list of orders for the specified customer
     */
    List<Order> findByCustomerEmailOrderByCreatedAtDesc(String customerEmail);

    /**
     * Find all orders by status with pagination support.
     * 
     * @param status the order status
     * @param pageable pagination information
     * @return page of orders with the specified status
     */
    Page<Order> findByStatusOrderByCreatedAtDesc(String status, Pageable pageable);

    /**
     * Find orders by customer email and status.
     * 
     * @param customerEmail the customer's email address
     * @param status the order status
     * @return list of orders matching the criteria
     */
    List<Order> findByCustomerEmailAndStatusOrderByCreatedAtDesc(String customerEmail, String status);

    /**
     * Find orders created between specified dates.
     * 
     * @param startDate the start date (inclusive)
     * @param endDate the end date (inclusive)
     * @param pageable pagination information
     * @return page of orders created within the date range
     */
    Page<Order> findByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    /**
     * Find orders with total amount greater than or equal to the specified minimum.
     * 
     * @param minAmount the minimum total amount
     * @param pageable pagination information
     * @return page of orders with total amount >= minAmount
     */
    Page<Order> findByTotalAmountGreaterThanEqualOrderByTotalAmountDesc(BigDecimal minAmount, Pageable pageable);

    /**
     * Find orders by customer email within a date range.
     * 
     * @param customerEmail the customer's email address
     * @param startDate the start date (inclusive)
     * @param endDate the end date (inclusive)
     * @return list of orders matching the criteria
     */
    List<Order> findByCustomerEmailAndCreatedAtBetweenOrderByCreatedAtDesc(
            String customerEmail, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Check if an order exists for the given customer email.
     * 
     * @param customerEmail the customer's email address
     * @return true if at least one order exists for the customer
     */
    boolean existsByCustomerEmail(String customerEmail);

    /**
     * Count total orders by status.
     * 
     * @param status the order status
     * @return count of orders with the specified status
     */
    long countByStatus(String status);

    /**
     * Find the most recent order for a customer.
     * 
     * @param customerEmail the customer's email address
     * @return optional containing the most recent order, or empty if none found
     */
    Optional<Order> findFirstByCustomerEmailOrderByCreatedAtDesc(String customerEmail);

    /**
     * Find orders with custom query for complex filtering.
     * Searches for orders by multiple criteria with flexible matching.
     * 
     * @param customerEmail the customer's email (can be null for no filter)
     * @param status the order status (can be null for no filter)
     * @param minAmount minimum total amount (can be null for no filter)
     * @param maxAmount maximum total amount (can be null for no filter)
     * @param startDate start date for created_at filter (can be null for no filter)
     * @param endDate end date for created_at filter (can be null for no filter)
     * @param pageable pagination information
     * @return page of orders matching the criteria
     */
    @Query("SELECT o FROM Order o WHERE " +
           "(:customerEmail IS NULL OR o.customerEmail = :customerEmail) AND " +
           "(:status IS NULL OR o.status = :status) AND " +
           "(:minAmount IS NULL OR o.totalAmount >= :minAmount) AND " +
           "(:maxAmount IS NULL OR o.totalAmount <= :maxAmount) AND " +
           "(:startDate IS NULL OR o.createdAt >= :startDate) AND " +
           "(:endDate IS NULL OR o.createdAt <= :endDate) " +
           "ORDER BY o.createdAt DESC")
    Page<Order> findOrdersWithFilters(
            @Param("customerEmail") String customerEmail,
            @Param("status") String status,
            @Param("minAmount") BigDecimal minAmount,
            @Param("maxAmount") BigDecimal maxAmount,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    /**
     * Get order statistics by customer email.
     * Returns total count and sum of order amounts for a customer.
     * 
     * @param customerEmail the customer's email address
     * @return array containing [count, total_amount] or null if no orders found
     */
    @Query("SELECT COUNT(o), COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.customerEmail = :customerEmail")
    Object[] getOrderStatsByCustomerEmail(@Param("customerEmail") String customerEmail);

    /**
     * Find orders that need processing (pending or confirmed status).
     * Useful for batch processing operations.
     * 
     * @return list of orders that need processing
     */
    @Query("SELECT o FROM Order o WHERE o.status IN ('PENDING', 'CONFIRMED') ORDER BY o.createdAt ASC")
    List<Order> findOrdersForProcessing();

    /**
     * Find top customers by order count within a date range.
     * 
     * @param startDate the start date (inclusive)
     * @param endDate the end date (inclusive)
     * @param pageable pagination information (use for limiting results)
     * @return list of arrays containing [customer_email, order_count]
     */
    @Query("SELECT o.customerEmail, COUNT(o) as orderCount FROM Order o " +
           "WHERE o.createdAt BETWEEN :startDate AND :endDate " +
           "GROUP BY o.customerEmail " +
           "ORDER BY orderCount DESC")
    Page<Object[]> findTopCustomersByOrderCount(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    /**
     * Delete orders older than the specified date.
     * Useful for data cleanup operations.
     * 
     * @param cutoffDate the cutoff date (orders older than this will be deleted)
     * @return number of deleted orders
     */
    long deleteByCreatedAtBefore(LocalDateTime cutoffDate);
}
```