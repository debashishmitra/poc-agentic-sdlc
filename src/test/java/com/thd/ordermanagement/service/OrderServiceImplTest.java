package com.thd.ordermanagement.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.thd.ordermanagement.dto.CreateOrderRequest;
import com.thd.ordermanagement.dto.OrderItemRequest;
import com.thd.ordermanagement.dto.OrderResponse;
import com.thd.ordermanagement.exception.InvalidOrderStateException;
import com.thd.ordermanagement.exception.OrderNotFoundException;
import com.thd.ordermanagement.model.Order;
import com.thd.ordermanagement.model.OrderItem;
import com.thd.ordermanagement.model.OrderStatus;
import com.thd.ordermanagement.repository.OrderRepository;

@ExtendWith(MockitoExtension.class)
public class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderServiceImpl orderService;

    private Order testOrder;
    private CreateOrderRequest createOrderRequest;
    private OrderItemRequest orderItemRequest;

    @BeforeEach
    void setUp() {
        testOrder = new Order();
        testOrder.setId(1L);
        testOrder.setCustomerName("John Doe");
        testOrder.setCustomerEmail("john@example.com");
        testOrder.setShippingAddress("123 Main St");
        testOrder.setOrderStatus(OrderStatus.PENDING);
        testOrder.setTotalAmount(new BigDecimal("99.99"));
        testOrder.setCreatedAt(LocalDateTime.now());
        testOrder.setUpdatedAt(LocalDateTime.now());

        OrderItem item = new OrderItem();
        item.setId(1L);
        item.setProductSku("SKU123");
        item.setProductName("Test Product");
        item.setQuantity(1);
        item.setUnitPrice(new BigDecimal("99.99"));
        item.setOrder(testOrder);

        testOrder.setOrderItems(Arrays.asList(item));

        orderItemRequest = new OrderItemRequest();
        orderItemRequest.setProductSku("SKU123");
        orderItemRequest.setProductName("Test Product");
        orderItemRequest.setQuantity(1);
        orderItemRequest.setUnitPrice(new BigDecimal("99.99"));

        createOrderRequest = new CreateOrderRequest();
        createOrderRequest.setCustomerName("John Doe");
        createOrderRequest.setCustomerEmail("john@example.com");
        createOrderRequest.setShippingAddress("123 Main St");
        createOrderRequest.setItems(Arrays.asList(orderItemRequest));
    }

    @Test
    void testCreateOrder_Success() {
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        OrderResponse response = orderService.createOrder(createOrderRequest);

        assertNotNull(response);
        assertEquals("John Doe", response.getCustomerName());
        assertEquals("john@example.com", response.getCustomerEmail());
        assertEquals(OrderStatus.PENDING, response.getOrderStatus());
        assertEquals(1, response.getOrderItems().size());
        assertEquals(new BigDecimal("99.99"), response.getTotalAmount());

        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void testCreateOrder_WithMultipleItems() {
        OrderItemRequest item1 = new OrderItemRequest();
        item1.setProductSku("SKU001");
        item1.setProductName("Product 1");
        item1.setQuantity(2);
        item1.setUnitPrice(new BigDecimal("50.00"));

        OrderItemRequest item2 = new OrderItemRequest();
        item2.setProductSku("SKU002");
        item2.setProductName("Product 2");
        item2.setQuantity(1);
        item2.setUnitPrice(new BigDecimal("99.99"));

        CreateOrderRequest multiItemRequest = new CreateOrderRequest();
        multiItemRequest.setCustomerName("John Doe");
        multiItemRequest.setCustomerEmail("john@example.com");
        multiItemRequest.setShippingAddress("123 Main St");
        multiItemRequest.setItems(Arrays.asList(item1, item2));

        Order savedOrder = new Order();
        savedOrder.setId(2L);
        savedOrder.setCustomerName("John Doe");
        savedOrder.setCustomerEmail("john@example.com");
        savedOrder.setShippingAddress("123 Main St");
        savedOrder.setOrderStatus(OrderStatus.PENDING);
        savedOrder.setTotalAmount(new BigDecimal("199.99"));
        savedOrder.setCreatedAt(LocalDateTime.now());
        savedOrder.setUpdatedAt(LocalDateTime.now());

        OrderItem orderItem1 = new OrderItem();
        orderItem1.setProductSku("SKU001");
        orderItem1.setProductName("Product 1");
        orderItem1.setQuantity(2);
        orderItem1.setUnitPrice(new BigDecimal("50.00"));

        OrderItem orderItem2 = new OrderItem();
        orderItem2.setProductSku("SKU002");
        orderItem2.setProductName("Product 2");
        orderItem2.setQuantity(1);
        orderItem2.setUnitPrice(new BigDecimal("99.99"));

        savedOrder.setOrderItems(Arrays.asList(orderItem1, orderItem2));

        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        OrderResponse response = orderService.createOrder(multiItemRequest);

        assertNotNull(response);
        assertEquals(2, response.getOrderItems().size());
        assertEquals(new BigDecimal("199.99"), response.getTotalAmount());

        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void testGetOrderById_Found() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        OrderResponse response = orderService.getOrderById(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("John Doe", response.getCustomerName());
        assertEquals("john@example.com", response.getCustomerEmail());

        verify(orderRepository, times(1)).findById(1L);
    }

    @Test
    void testGetOrderById_NotFound() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class, () -> orderService.getOrderById(999L));

        verify(orderRepository, times(1)).findById(999L);
    }

    @Test
    void testGetAllOrders() {
        Order order2 = new Order();
        order2.setId(2L);
        order2.setCustomerName("Jane Doe");
        order2.setCustomerEmail("jane@example.com");
        order2.setShippingAddress("456 Oak St");
        order2.setOrderStatus(OrderStatus.CONFIRMED);
        order2.setTotalAmount(new BigDecimal("199.99"));
        order2.setCreatedAt(LocalDateTime.now());
        order2.setUpdatedAt(LocalDateTime.now());
        order2.setOrderItems(Arrays.asList());

        when(orderRepository.findAll()).thenReturn(Arrays.asList(testOrder, order2));

        List<OrderResponse> responses = orderService.getAllOrders();

        assertNotNull(responses);
        assertEquals(2, responses.size());
        assertEquals("John Doe", responses.get(0).getCustomerName());
        assertEquals("Jane Doe", responses.get(1).getCustomerName());

        verify(orderRepository, times(1)).findAll();
    }

    @Test
    void testGetAllOrders_EmptyList() {
        when(orderRepository.findAll()).thenReturn(Arrays.asList());

        List<OrderResponse> responses = orderService.getAllOrders();

        assertNotNull(responses);
        assertEquals(0, responses.size());

        verify(orderRepository, times(1)).findAll();
    }

    @Test
    void testUpdateOrderStatus_ValidTransition() {
        Order orderToUpdate = new Order();
        orderToUpdate.setId(1L);
        orderToUpdate.setCustomerName("John Doe");
        orderToUpdate.setCustomerEmail("john@example.com");
        orderToUpdate.setShippingAddress("123 Main St");
        orderToUpdate.setOrderStatus(OrderStatus.PENDING);
        orderToUpdate.setTotalAmount(new BigDecimal("99.99"));
        orderToUpdate.setCreatedAt(LocalDateTime.now());
        orderToUpdate.setUpdatedAt(LocalDateTime.now());
        orderToUpdate.setOrderItems(Arrays.asList());

        when(orderRepository.findById(1L)).thenReturn(Optional.of(orderToUpdate));
        when(orderRepository.save(any(Order.class))).thenReturn(orderToUpdate);

        OrderResponse response = orderService.updateOrderStatus(1L, OrderStatus.CONFIRMED);

        assertNotNull(response);
        assertEquals(OrderStatus.CONFIRMED, response.getOrderStatus());

        verify(orderRepository, times(1)).findById(1L);
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void testUpdateOrderStatus_InvalidTransition_FromCancelled() {
        Order cancelledOrder = new Order();
        cancelledOrder.setId(1L);
        cancelledOrder.setOrderStatus(OrderStatus.CANCELLED);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(cancelledOrder));

        assertThrows(InvalidOrderStateException.class,
                () -> orderService.updateOrderStatus(1L, OrderStatus.CONFIRMED));

        verify(orderRepository, times(1)).findById(1L);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void testUpdateOrderStatus_InvalidTransition_FromDelivered() {
        Order deliveredOrder = new Order();
        deliveredOrder.setId(1L);
        deliveredOrder.setOrderStatus(OrderStatus.DELIVERED);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(deliveredOrder));

        assertThrows(InvalidOrderStateException.class,
                () -> orderService.updateOrderStatus(1L, OrderStatus.CONFIRMED));

        verify(orderRepository, times(1)).findById(1L);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void testUpdateOrderStatus_InvalidTransition_PendingToShipped() {
        Order pendingOrder = new Order();
        pendingOrder.setId(1L);
        pendingOrder.setOrderStatus(OrderStatus.PENDING);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(pendingOrder));

        assertThrows(InvalidOrderStateException.class,
                () -> orderService.updateOrderStatus(1L, OrderStatus.SHIPPED));

        verify(orderRepository, times(1)).findById(1L);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void testUpdateOrderStatus_OrderNotFound() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class,
                () -> orderService.updateOrderStatus(999L, OrderStatus.CONFIRMED));

        verify(orderRepository, times(1)).findById(999L);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void testCancelOrder_Success() {
        Order pendingOrder = new Order();
        pendingOrder.setId(1L);
        pendingOrder.setOrderStatus(OrderStatus.PENDING);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(pendingOrder));

        orderService.cancelOrder(1L);

        assertEquals(OrderStatus.CANCELLED, pendingOrder.getOrderStatus());
        verify(orderRepository, times(1)).findById(1L);
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void testCancelOrder_AlreadyCancelled() {
        Order cancelledOrder = new Order();
        cancelledOrder.setId(1L);
        cancelledOrder.setOrderStatus(OrderStatus.CANCELLED);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(cancelledOrder));

        assertThrows(InvalidOrderStateException.class, () -> orderService.cancelOrder(1L));

        verify(orderRepository, times(1)).findById(1L);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void testCancelOrder_AlreadyShipped() {
        Order shippedOrder = new Order();
        shippedOrder.setId(1L);
        shippedOrder.setOrderStatus(OrderStatus.SHIPPED);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(shippedOrder));

        assertThrows(InvalidOrderStateException.class, () -> orderService.cancelOrder(1L));

        verify(orderRepository, times(1)).findById(1L);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void testCancelOrder_AlreadyDelivered() {
        Order deliveredOrder = new Order();
        deliveredOrder.setId(1L);
        deliveredOrder.setOrderStatus(OrderStatus.DELIVERED);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(deliveredOrder));

        assertThrows(InvalidOrderStateException.class, () -> orderService.cancelOrder(1L));

        verify(orderRepository, times(1)).findById(1L);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void testCancelOrder_OrderNotFound() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class, () -> orderService.cancelOrder(999L));

        verify(orderRepository, times(1)).findById(999L);
        verify(orderRepository, never()).save(any(Order.class));
    }
}
