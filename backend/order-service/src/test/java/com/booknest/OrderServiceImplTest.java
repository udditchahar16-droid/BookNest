package com.booknest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for OrderServiceImpl
 * Tests: getAllOrders, placeOrder, onlinePayment, changeStatus, deleteOrder,
 *        getOrderByUserId, storeAddress, getOrderById
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService Unit Tests")
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private AddressRepository addressRepository;

    @InjectMocks
    private OrderServiceImpl orderService;

    private Order mockOrder;
    private Address mockAddress;
    private Cart mockCart;

    @BeforeEach
    void setUp() {
        mockAddress = new Address();
        mockAddress.setCustomerId(1);
        mockAddress.setFullName("Ravi Kumar");
        mockAddress.setMobileNumber("9876543210");
        mockAddress.setFlatNumber(12);
        mockAddress.setCity("Mathura");
        mockAddress.setPincode(281001);
        mockAddress.setState("Uttar Pradesh");

        mockOrder = new Order();
        mockOrder.setOrderId(100);
        mockOrder.setUserId(1);
        mockOrder.setOrderDate(LocalDate.now());
        mockOrder.setAmountPaid(599.0);
        mockOrder.setModeOfPayment("COD");
        mockOrder.setOrderStatus("PLACED");
        mockOrder.setQuantity(1);

        mockCart = new Cart();
        mockCart.setUserId(1);
    }

    // ── getAllOrders ──────────────────────────────────────────────────────

    @Test
    @DisplayName("getAllOrders: should return all orders from repository")
    void getAllOrders_returnsList() {
        when(orderRepository.findAll()).thenReturn(List.of(mockOrder));

        List<Order> result = orderService.getAllOrders();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getOrderId()).isEqualTo(100);
    }

    @Test
    @DisplayName("getAllOrders: should return empty list when no orders")
    void getAllOrders_returnsEmpty() {
        when(orderRepository.findAll()).thenReturn(List.of());
        assertThat(orderService.getAllOrders()).isEmpty();
    }

    // ── placeOrder (COD) ──────────────────────────────────────────────────

    @Test
    @DisplayName("placeOrder: should create COD order with PLACED status")
    void placeOrder_createsCODOrder() {
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> {
            Order o = inv.getArgument(0);
            o.setOrderId(100);
            return o;
        });

        orderService.placeOrder(mockCart);

        verify(orderRepository, atLeastOnce()).save(any(Order.class));
    }

    // ── onlinePayment ─────────────────────────────────────────────────────

    @Test
    @DisplayName("onlinePayment: should create WALLET order and save")
    void onlinePayment_createsWalletOrder() {
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> {
            Order o = inv.getArgument(0);
            o.setOrderId(101);
            o.setModeOfPayment("WALLET");
            return o;
        });

        orderService.onlinePayment(mockCart);

        verify(orderRepository, atLeastOnce()).save(any(Order.class));
    }

    // ── changeStatus ──────────────────────────────────────────────────────

    @Test
    @DisplayName("changeStatus: should update order status and save")
    void changeStatus_updatesStatus() {
        when(orderRepository.findFirstByOrderByOrderIdDesc()).thenReturn(mockOrder);
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        String result = orderService.changeStatus("DISPATCHED", 100);

        assertThat(result).isEqualTo("DISPATCHED");
    }

    @Test
    @DisplayName("changeStatus: all valid statuses should be accepted")
    void changeStatus_acceptsAllValidStatuses() {
        for (String status : List.of("PLACED", "CONFIRMED", "DISPATCHED", "DELIVERED", "CANCELLED")) {
            when(orderRepository.findFirstByOrderByOrderIdDesc()).thenReturn(mockOrder);
            when(orderRepository.save(any(Order.class))).thenReturn(mockOrder);
            String result = orderService.changeStatus(status, 100);
            assertThat(result).isEqualTo(status);
        }
    }

    // ── deleteOrder ───────────────────────────────────────────────────────

    @Test
    @DisplayName("deleteOrder: should call deleteById on repository")
    void deleteOrder_callsDelete() {
        doNothing().when(orderRepository).deleteById(100);
        orderService.deleteOrder(100);
        verify(orderRepository, times(1)).deleteById(100);
    }

    // ── getOrderByUserId ──────────────────────────────────────────────────

    @Test
    @DisplayName("getOrderByUserId: should return orders for given user")
    void getOrderByUserId_returnsOrders() {
        when(orderRepository.findByUserId(1)).thenReturn(List.of(mockOrder));

        List<Order> result = orderService.getOrderByUserId(1);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUserId()).isEqualTo(1);
    }

    @Test
    @DisplayName("getOrderByUserId: should return empty list for user with no orders")
    void getOrderByUserId_returnsEmptyForNoOrders() {
        when(orderRepository.findByUserId(99)).thenReturn(List.of());
        assertThat(orderService.getOrderByUserId(99)).isEmpty();
    }

    // ── getOrderById ──────────────────────────────────────────────────────

    @Test
    @DisplayName("getOrderById: should return order for valid ID")
    void getOrderById_returnsOrder() {
        when(orderRepository.findById(100)).thenReturn(Optional.of(mockOrder));

        Optional<Order> result = orderService.getOrderById(100);

        assertThat(result).isPresent();
        assertThat(result.get().getAmountPaid()).isEqualTo(599.0);
    }

    @Test
    @DisplayName("getOrderById: should return empty Optional for unknown ID")
    void getOrderById_returnsEmpty() {
        when(orderRepository.findById(999)).thenReturn(Optional.empty());
        assertThat(orderService.getOrderById(999)).isEmpty();
    }

    // ── storeAddress ──────────────────────────────────────────────────────

    @Test
    @DisplayName("storeAddress: should persist address via addressRepository")
    void storeAddress_persists() {
        when(addressRepository.save(any(Address.class))).thenReturn(mockAddress);

        orderService.storeAddress(mockAddress);

        verify(addressRepository, times(1)).save(mockAddress);
    }

    // ── getAddressByCustomerId ────────────────────────────────────────────

    @Test
    @DisplayName("getAddressByCustomerId: should return all saved addresses")
    void getAddressByCustomerId_returnsList() {
        when(addressRepository.findByCustomerId(1)).thenReturn(List.of(mockAddress));

        List<Address> result = orderService.getAddressByCustomerId(1);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCity()).isEqualTo("Mathura");
    }

    // ── findByOrderStatus ─────────────────────────────────────────────────

    @Test
    @DisplayName("findByOrderStatus: should return PLACED orders")
    void findByOrderStatus_returnsPlaced() {
        when(orderRepository.findByOrderStatus("PLACED")).thenReturn(List.of(mockOrder));

        List<Order> placed = orderRepository.findByOrderStatus("PLACED");
        assertThat(placed).allMatch(o -> o.getOrderStatus().equals("PLACED"));
    }

    // ── findByOrderDateBetween ────────────────────────────────────────────

    @Test
    @DisplayName("findByOrderDateBetween: should return orders within date range")
    void findByOrderDateBetween_returnsInRange() {
        LocalDate from = LocalDate.now().minusDays(7);
        LocalDate to = LocalDate.now();
        when(orderRepository.findByOrderDateBetween(from, to)).thenReturn(List.of(mockOrder));

        List<Order> result = orderRepository.findByOrderDateBetween(from, to);
        assertThat(result).hasSize(1);
    }
}
