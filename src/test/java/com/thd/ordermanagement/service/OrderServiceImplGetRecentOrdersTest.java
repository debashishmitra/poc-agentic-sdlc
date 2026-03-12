package com.thd.ordermanagement.service;

import com.thd.ordermanagement.dto.OrderResponse;
import com.thd.ordermanagement.dto.RecentOrdersResponse;
import com.thd.ordermanagement.model.Order;
import com.thd.ordermanagement.model.OrderItem;
import com.thd.ordermanagement.model.OrderStatus;
import com.thd.ordermanagement.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceImplGetRecentOrdersTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderServiceImpl orderService;

    private Order testOrder1;
    private Order testOrder2;
    private Order testOrder3;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();

        testOrder1 = new Order();
        testOrder1.setId(1L);
        testOrder1.setCustomerName("John Doe");
        testOrder1.setCustomerEmail("john@example.com");
        testOrder1.setShippingAddress("123 Main St");
        testOrder1.setOrderStatus(OrderStatus.PENDING);
        testOrder1.setTotalAmount(new BigDecimal("99.99"));
        testOrder1.setCreatedAt(now.minusMinutes(30));
        testOrder1.setUpdatedAt(now.minusMinutes(30));

        OrderItem item1 = new OrderItem();
        item1.setId(1L);
        item1.setProductSku("SKU001");
        item1.setProductName("Product 1");
        item1.setQuantity(1);
        item1.setUnitPrice(new BigDecimal("99.99"));
        item1.setOrder(testOrder1);
        testOrder1.setOrderItems(Arrays.asList(item1));

        testOrder2 = new Order();
        testOrder2.setId(2L);
        testOrder2.setCustomerName("Jane Smith");
        testOrder2.setCustomerEmail("jane@example.com");
        testOrder2.setShippingAddress("456 Oak Ave");
        testOrder2.setOrderStatus(OrderStatus.CONFIRMED);
        testOrder2.setTotalAmount(new BigDecimal("149.50"));
        testOrder2.setCreatedAt(now.minusMinutes(15));
        testOrder2.setUpdatedAt(now.minusMinutes(15));

        OrderItem item2 = new OrderItem();
        item2.setId(2L);
        item2.setProductSku("SKU002");
        item2.setProductName("Product 2");
        item2.setQuantity(2);
        item2.setUnitPrice(new BigDecimal("74.75"));
        item2.setOrder(testOrder2);
        testOrder2.setOrderItems(Arrays.asList(item2));

        testOrder3 = new Order();
        testOrder3.setId(3L);
        testOrder3.setCustomerName("Bob Johnson");
        testOrder3.setCustomerEmail("bob@example.com");
        testOrder3.setShippingAddress("789 Pine Rd");
        testOrder3.setOrderStatus(OrderStatus.PENDING);
        testOrder3.setTotalAmount(new BigDecimal("250.00"));
        testOrder3.setCreatedAt(now);
        testOrder3.setUpdatedAt(now);

        OrderItem item3 = new OrderItem();
        item3.setId(3L);
        item3.setProductSku("SKU003");
        item3.setProductName("Product 3");
        item3.setQuantity(5);
        item3.setUnitPrice(new BigDecimal("50.00"));
        item3.setOrder(testOrder3);
        testOrder3.setOrderItems(Arrays.asList(item3));
    }

    @Test
    void should_returnRecentOrders_when_defaultLimitUsed() {
        List<Order> orders = Arrays.asList(testOrder3, testOrder2, testOrder1);
        when(orderRepository.findAllByOrderByCreatedAtDesc(any(Pageable.class))).thenReturn(orders);

        RecentOrdersResponse response = orderService.getRecentOrders(10);

        assertNotNull(response);
        assertEquals(3, response.getCount());
        assertEquals(3, response.getOrders().size());
        assertEquals("Bob Johnson", response.getOrders().get(0).getCustomerName());
        assertEquals("Jane Smith", response.getOrders().get(1).getCustomerName());
        assertEquals("John Doe", response.getOrders().get(2).getCustomerName());

        verify(orderRepository, times(1)).findAllByOrderByCreatedAtDesc(any(Pageable.class));
    }

    @Test
    void should_returnEmptyListAndZeroCount_when_noOrdersExist() {
        when(orderRepository.findAllByOrderByCreatedAtDesc(any(Pageable.class))).thenReturn(Collections.emptyList());

        RecentOrdersResponse response = orderService.getRecentOrders(10);

        assertNotNull(response);
        assertNotNull(response.getOrders());
        assertEquals(0, response.getCount());
        assertTrue(response.getOrders().isEmpty());

        verify(orderRepository, times(1)).findAllByOrderByCreatedAtDesc(any(Pageable.class));
    }

    @Test
    void should_returnLimitedOrders_when_customLimitProvided() {
        List<Order> orders = Arrays.asList(testOrder3);
        when(orderRepository.findAllByOrderByCreatedAtDesc(any(Pageable.class))).thenReturn(orders);

        RecentOrdersResponse response = orderService.getRecentOrders(1);

        assertNotNull(response);
        assertEquals(1, response.getCount());
        assertEquals(1, response.getOrders().size());
        assertEquals("Bob Johnson", response.getOrders().get(0).getCustomerName());

        verify(orderRepository, times(1)).findAllByOrderByCreatedAtDesc(any(Pageable.class));
    }

    @Test
    void should_returnCorrectCount_when_fewerOrdersThanLimit() {
        List<Order> orders = Arrays.asList(testOrder3, testOrder2);
        when(orderRepository.findAllByOrderByCreatedAtDesc(any(Pageable.class))).thenReturn(orders);

        RecentOrdersResponse response = orderService.getRecentOrders(50);

        assertNotNull(response);
        assertEquals(2, response.getCount());
        assertEquals(2, response.getOrders().size());

        verify(orderRepository, times(1)).findAllByOrderByCreatedAtDesc(any(Pageable.class));
    }

    @Test
    void should_returnSingleOrder_when_limitIsOne() {
        List<Order> orders = Arrays.asList(testOrder3);
        when(orderRepository.findAllByOrderByCreatedAtDesc(any(Pageable.class))).thenReturn(orders);

        RecentOrdersResponse response = orderService.getRecentOrders(1);

        assertNotNull(response);
        assertEquals(1, response.getCount());
        assertEquals(1, response.getOrders().size());
        assertEquals(3L, response.getOrders().get(0).getId());

        verify(orderRepository, times(1)).findAllByOrderByCreatedAtDesc(any(Pageable.class));
    }

    @Test
    void should_returnMaxOrders_when_limitIsFifty() {
        List<Order> fiftyOrders = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        for (int i = 50; i >= 1; i--) {
            Order order = new Order();
            order.setId((long) i);
            order.setCustomerName("Customer " + i);
            order.setCustomerEmail("customer" + i + "@example.com");
            order.setShippingAddress("Address " + i);
            order.setOrderStatus(OrderStatus.PENDING);
            order.setTotalAmount(new BigDecimal("10.00"));
            order.setCreatedAt(now.minusMinutes(50 - i));
            order.setUpdatedAt(now.minusMinutes(50 - i));

            OrderItem item = new OrderItem();
            item.setId((long) i);
            item.setProductSku("SKU" + i);
            item.setProductName("Product " + i);
            item.setQuantity(1);
            item.setUnitPrice(new BigDecimal("10.00"));
            item.setOrder(order);
            order.setOrderItems(Arrays.asList(item));

            fiftyOrders.add(order);
        }
        when(orderRepository.findAllByOrderByCreatedAtDesc(any(Pageable.class))).thenReturn(fiftyOrders);

        RecentOrdersResponse response = orderService.getRecentOrders(50);

        assertNotNull(response);
        assertEquals(50, response.getCount());
        assertEquals(50, response.getOrders().size());

        verify(orderRepository, times(1)).findAllByOrderByCreatedAtDesc(any(Pageable.class));
    }

    @Test
    void should_returnCorrectOrderDetails_when_ordersExist() {
        List<Order> orders = Arrays.asList(testOrder3);
        when(orderRepository.findAllByOrderByCreatedAtDesc(any(Pageable.class))).thenReturn(orders);

        RecentOrdersResponse response = orderService.getRecentOrders(10);

        assertNotNull(response);
        assertEquals(1, response.getCount());

        OrderResponse orderResponse = response.getOrders().get(0);
        assertEquals(3L, orderResponse.getId());
        assertEquals("Bob Johnson", orderResponse.getCustomerName());
        assertEquals("bob@example.com", orderResponse.getCustomerEmail());
        assertEquals("789 Pine Rd", orderResponse.getShippingAddress());
        assertEquals(OrderStatus.PENDING, orderResponse.getOrderStatus());
        assertEquals(new BigDecimal("250.00"), orderResponse.getTotalAmount());
        assertNotNull(orderResponse.getOrderItems());
        assertEquals(1, orderResponse.getOrderItems().size());
        assertEquals("SKU003", orderResponse.getOrderItems().get(0).getProductSku());
        assertEquals("Product 3", orderResponse.getOrderItems().get(0).getProductName());
        assertEquals(5, orderResponse.getOrderItems().get(0).getQuantity());
        assertEquals(new BigDecimal("50.00"), orderResponse.getOrderItems().get(0).getUnitPrice());

        verify(orderRepository, times(1)).findAllByOrderByCreatedAtDesc(any(Pageable.class));
    }

    @Test
    void should_preserveDescendingOrder_when_multipleOrdersReturned() {
        List<Order> orders = Arrays.asList(testOrder3, testOrder2, testOrder1);
        when(orderRepository.findAllByOrderByCreatedAtDesc(any(Pageable.class))).thenReturn(orders);

        RecentOrdersResponse response = orderService.getRecentOrders(10);

        assertNotNull(response);
        assertEquals(3, response.getCount());

        List<OrderResponse> responseOrders = response.getOrders();
        assertEquals(3L, responseOrders.get(0).getId());
        assertEquals(2L, responseOrders.get(1).getId());
        assertEquals(1L, responseOrders.get(2).getId());

        assertTrue(responseOrders.get(0).getCreatedAt().isAfter(responseOrders.get(1).getCreatedAt())
                || responseOrders.get(0).getCreatedAt().isEqual(responseOrders.get(1).getCreatedAt()));
        assertTrue(responseOrders.get(1).getCreatedAt().isAfter(responseOrders.get(2).getCreatedAt())
                || responseOrders.get(1).getCreatedAt().isEqual(responseOrders.get(2).getCreatedAt()));

        verify(orderRepository, times(1)).findAllByOrderByCreatedAtDesc(any(Pageable.class));
    }

    @Test
    void should_returnOrdersWithDifferentStatuses_when_mixedStatusesExist() {
        List<Order> orders = Arrays.asList(testOrder3, testOrder2);
        when(orderRepository.findAllByOrderByCreatedAtDesc(any(Pageable.class))).thenReturn(orders);

        RecentOrdersResponse response = orderService.getRecentOrders(10);

        assertNotNull(response);
        assertEquals(2, response.getCount());
        assertEquals(OrderStatus.PENDING, response.getOrders().get(0).getOrderStatus());
        assertEquals(OrderStatus.CONFIRMED, response.getOrders().get(1).getOrderStatus());

        verify(orderRepository, times(1)).findAllByOrderByCreatedAtDesc(any(Pageable.class));
    }

    @Test
    void should_returnCountMatchingListSize_when_ordersReturned() {
        List<Order> orders = Arrays.asList(testOrder3, testOrder2, testOrder1);
        when(orderRepository.findAllByOrderByCreatedAtDesc(any(Pageable.class))).thenReturn(orders);

        RecentOrdersResponse response = orderService.getRecentOrders(10);

        assertNotNull(response);
        assertEquals(response.getOrders().size(), response.getCount());

        verify(orderRepository, times(1)).findAllByOrderByCreatedAtDesc(any(Pageable.class));
    }

    @Test
    void should_passCorrectPageable_when_limitIsDefault() {
        when(orderRepository.findAllByOrderByCreatedAtDesc(any(Pageable.class))).thenReturn(Collections.emptyList());

        orderService.getRecentOrders(10);

        verify(orderRepository, times(1)).findAllByOrderByCreatedAtDesc(argThat(pageable ->
                pageable.getPageNumber() == 0 && pageable.getPageSize() == 10
        ));
    }

    @Test
    void should_passCorrectPageable_when_customLimitProvided() {
        when(orderRepository.findAllByOrderByCreatedAtDesc(any(Pageable.class))).thenReturn(Collections.emptyList());

        orderService.getRecentOrders(25);

        verify(orderRepository, times(1)).findAllByOrderByCreatedAtDesc(argThat(pageable ->
                pageable.getPageNumber() == 0 && pageable.getPageSize() == 25
        ));
    }

    @Test
    void should_passCorrectPageable_when_limitIsMinimum() {
        when(orderRepository.findAllByOrderByCreatedAtDesc(any(Pageable.class))).thenReturn(Collections.emptyList());

        orderService.getRecentOrders(1);

        verify(orderRepository, times(1)).findAllByOrderByCreatedAtDesc(argThat(pageable ->
                pageable.getPageNumber() == 0 && pageable.getPageSize() == 1
        ));
    }

    @Test
    void should_passCorrectPageable_when_limitIsMaximum() {
        when(orderRepository.findAllByOrderByCreatedAtDesc(any(Pageable.class))).thenReturn(Collections.emptyList());

        orderService.getRecentOrders(50);

        verify(orderRepository, times(1)).findAllByOrderByCreatedAtDesc(argThat(pageable ->
                pageable.getPageNumber() == 0 && pageable.getPageSize() == 50
        ));
    }

    @Test
    void should_returnOrderWithMultipleItems_when_orderHasMultipleItems() {
        Order multiItemOrder = new Order();
        multiItemOrder.setId(10L);
        multiItemOrder.setCustomerName("Multi Item Customer");
        multiItemOrder.setCustomerEmail("multi@example.com");
        multiItemOrder.setShippingAddress("999 Elm St");
        multiItemOrder.setOrderStatus(OrderStatus.PENDING);
        multiItemOrder.setTotalAmount(new BigDecimal("199.98"));
        multiItemOrder.setCreatedAt(LocalDateTime.now());
        multiItemOrder.setUpdatedAt(LocalDateTime.now());

        OrderItem itemA = new OrderItem();
        itemA.setId(10L);
        itemA.setProductSku("SKUA");
        itemA.setProductName("Product A");
        itemA.setQuantity(2);
        itemA.setUnitPrice(new BigDecimal("49.99"));
        itemA.setOrder(multiItemOrder);

        OrderItem itemB = new OrderItem();
        itemB.setId(11L);
        itemB.setProductSku("SKUB");
        itemB.setProductName("Product B");
        itemB.setQuantity(1);
        itemB.setUnitPrice(new BigDecimal("100.00"));
        itemB.setOrder(multiItemOrder);

        multiItemOrder.setOrderItems(Arrays.asList(itemA, itemB));

        when(orderRepository.findAllByOrderByCreatedAtDesc(any(Pageable.class))).thenReturn(Arrays.asList(multiItemOrder));

        RecentOrdersResponse response = orderService.getRecentOrders(10);

        assertNotNull(response);
        assertEquals(1, response.getCount());
        OrderResponse orderResponse = response.getOrders().get(0);
        assertEquals(2, orderResponse.getOrderItems().size());
        assertEquals(new BigDecimal("199.98"), orderResponse.getTotalAmount());

        verify(orderRepository, times(1)).findAllByOrderByCreatedAtDesc(any(Pageable.class));
    }

    @Test
    void should_returnNotNullOrdersList_when_emptyResultFromRepository() {
        when(orderRepository.findAllByOrderByCreatedAtDesc(any(Pageable.class))).thenReturn(Collections.emptyList());

        RecentOrdersResponse response = orderService.getRecentOrders(10);

        assertNotNull(response);
        assertNotNull(response.getOrders());
        assertEquals(0, response.getCount());
        assertEquals(0, response.getOrders().size());

        verify(orderRepository, times(1)).findAllByOrderByCreatedAtDesc(any(Pageable.class));
    }

    @Test
    void should_returnCorrectTimestamps_when_ordersHaveCreatedAt() {
        LocalDateTime specificTime = LocalDateTime.of(2025, 1, 15, 10, 30, 0);
        testOrder1.setCreatedAt(specificTime);
        testOrder1.setUpdatedAt(specificTime);

        when(orderRepository.findAllByOrderByCreatedAtDesc(any(Pageable.class))).thenReturn(Arrays.asList(testOrder1));

        RecentOrdersResponse response = orderService.getRecentOrders(10);

        assertNotNull(response);
        assertEquals(1, response.getCount());
        assertNotNull(response.getOrders().get(0).getCreatedAt());
        assertEquals(specificTime, response.getOrders().get(0).getCreatedAt());

        verify(orderRepository, times(1)).findAllByOrderByCreatedAtDesc(any(Pageable.class));
    }

    @Test
    void should_invokeRepositoryOnce_when_getRecentOrdersCalled() {
        when(orderRepository.findAllByOrderByCreatedAtDesc(any(Pageable.class))).thenReturn(Collections.emptyList());

        orderService.getRecentOrders(5);

        verify(orderRepository, times(1)).findAllByOrderByCreatedAtDesc(any(Pageable.class));
        verifyNoMoreInteractions(orderRepository);
    }
}