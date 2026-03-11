

```java
package com.thd.ordermanagement.repository;

import com.thd.ordermanagement.entity.Order;
import com.thd.ordermanagement.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA Repository for the {@link Order} entity.
 *
 * <p>Provides standard CRUD operations via {@link JpaRepository} and custom
 * query methods to support the order count summary endpoint
 * ({@code GET /api/v1/orders/summary/counts}).
 *
 * <p>The {@link #countOrdersGroupedByStatus()} method uses a JPQL aggregate
 * query that returns each {@link OrderStatus} paired with its count, which
 * the service layer transforms into the response DTO.
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * Returns order counts grouped by {@link OrderStatus}.
     *
     * <p>Each element in the returned list is an {@code Object[]} where:
     * <ul>
     *   <li>{@code [0]} — {@link OrderStatus} enum value</li>
     *   <li>{@code [1]} — {@link Long} count of orders with that status</li>
     * </ul>
     *
     * <p>Statuses with zero orders will <em>not</em> appear in the result set;
     * the service layer is responsible for defaulting missing statuses to {@code 0}.
     *
     * @return a list of status/count pairs
     */
    @Query("SELECT o.status, COUNT(o) FROM Order o GROUP BY o.status")
    List<Object[]> countOrdersGroupedByStatus();

    /**
     * Counts the total number of orders with the given status.
     *
     * @param status the {@link OrderStatus} to filter by
     * @return the number of orders matching the specified status
     */
    long countByStatus(OrderStatus status);
}
```