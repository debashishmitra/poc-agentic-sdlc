

```java
package com.thd.ordermanagement.service;

import com.thd.ordermanagement.dto.OrderCountSummaryDTO;
import com.thd.ordermanagement.model.OrderStatus;
import com.thd.ordermanagement.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service responsible for providing order summary and reporting operations.
 * <p>
 * This service encapsulates the business logic for aggregating order data,
 * specifically computing order counts grouped by {@link OrderStatus}.
 * </p>
 */
@Service
public class OrderSummaryService {

    private static final Logger log = LoggerFactory.getLogger(OrderSummaryService.class);

    private final OrderRepository orderRepository;

    /**
     * Constructs an {@code OrderSummaryService} with the required repository dependency.
     *
     * @param orderRepository the repository used to query order data
     */
    public OrderSummaryService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    /**
     * Retrieves a summary of order counts grouped by {@link OrderStatus}.
     * <p>
     * This method queries the database for order counts per status and ensures that
     * every defined {@code OrderStatus} enum value is represented in the result,
     * defaulting to {@code 0} for any status that has no matching orders.
     * The total order count is computed as the sum of all individual status counts.
     * </p>
     *
     * @return an {@link OrderCountSummaryDTO} containing a map of status-to-count
     *         entries and the total number of orders
     */
    @Transactional(readOnly = true)
    public OrderCountSummaryDTO getOrderCountSummary() {
        log.info("Retrieving order count summary grouped by status");

        // Query the repository for raw status-count pairs
        List<Object[]> rawCounts = orderRepository.countOrdersGroupedByStatus();
        log.debug("Received {} status group(s) from repository", rawCounts.size());

        // Convert raw query results into a map keyed by OrderStatus
        Map<OrderStatus, Long> queriedCounts = rawCounts.stream()
                .collect(Collectors.toMap(
                        row -> (OrderStatus) row[0],
                        row -> (Long) row[1]
                ));

        // Build the final status counts map ensuring every OrderStatus is present.
        // We use a LinkedHashMap to maintain a predictable, consistent ordering
        // that follows the enum declaration order.
        Map<OrderStatus, Long> statusCounts = Arrays.stream(OrderStatus.values())
                .collect(Collectors.toMap(
                        status -> status,
                        status -> queriedCounts.getOrDefault(status, 0L),
                        (existing, replacement) -> existing,
                        LinkedHashMap::new
                ));

        // Compute the total across all statuses
        long totalOrders = statusCounts.values().stream()
                .mapToLong(Long::longValue)
                .sum();

        log.info("Order count summary computed successfully: totalOrders={}", totalOrders);

        if (log.isDebugEnabled()) {
            statusCounts.forEach((status, count) ->
                    log.debug("Status [{}]: {} order(s)", status, count));
        }

        // Build and return the DTO
        OrderCountSummaryDTO summary = new OrderCountSummaryDTO();
        summary.setStatusCounts(statusCounts);
        summary.setTotalOrders(totalOrders);

        return summary;
    }
}
```