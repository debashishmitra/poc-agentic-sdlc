

```java
package com.thd.ordermanagement.controller;

import com.thd.ordermanagement.dto.OrderCountSummaryResponse;
import com.thd.ordermanagement.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller for Order-related endpoints.
 * 
 * Provides endpoints for order management operations including
 * summary and reporting capabilities.
 */
@RestController
@RequestMapping("/api/v1/orders")
@Tag(name = "Orders", description = "Order management and reporting endpoints")
public class OrderController {

    private static final Logger log = LoggerFactory.getLogger(OrderController.class);

    private final OrderService orderService;

    /**
     * Constructs the OrderController with required dependencies.
     *
     * @param orderService the order service for business logic operations
     */
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * Retrieves a summary of order counts grouped by {@code OrderStatus}.
     * 
     * <p>Returns a count for every possible order status, defaulting to 0
     * for statuses that have no associated orders. Also includes a total
     * order count across all statuses.</p>
     *
     * @return a {@link ResponseEntity} containing the {@link OrderCountSummaryResponse}
     *         with status counts and total order count
     */
    @GetMapping("/summary/counts")
    @Operation(
            summary = "Get order count summary",
            description = "Returns the count of orders grouped by each OrderStatus along with a total order count. "
                    + "Every status value is always present in the response; statuses with no orders default to 0."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved order count summary",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = OrderCountSummaryResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error occurred while retrieving order counts",
                    content = @Content
            )
    })
    public ResponseEntity<OrderCountSummaryResponse> getOrderCountSummary() {
        log.info("Received request to get order count summary");

        try {
            OrderCountSummaryResponse summary = orderService.getOrderCountSummary();

            log.info("Successfully retrieved order count summary. Total orders: {}", summary.getTotalOrders());
            log.debug("Order count summary details: {}", summary.getStatusCounts());

            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            log.error("Error occurred while retrieving order count summary", e);
            throw e;
        }
    }
}
```