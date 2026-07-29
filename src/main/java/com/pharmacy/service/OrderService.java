package com.pharmacy.service;

import com.pharmacy.entity.*;
import com.pharmacy.exception.ResourceNotFoundException;
import com.pharmacy.repository.OrderRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class OrderService {
    
    private final OrderRepository orderRepository;
    private final CartService cartService;
    private final MedicineService medicineService;
    private final InvoiceService invoiceService;
    
    public OrderService(OrderRepository orderRepository, CartService cartService, 
                        MedicineService medicineService, InvoiceService invoiceService) {
        this.orderRepository = orderRepository;
        this.cartService = cartService;
        this.medicineService = medicineService;
        this.invoiceService = invoiceService;
    }
    
    @Transactional
    @SuppressWarnings("null")
    public Order createOrderWithPayment(User user, String paymentMethod) {
        List<Cart> cartItems = cartService.getCartItems(user);
        
        if (cartItems.isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }
        
        BigDecimal totalAmount = cartService.getCartTotal(user);
        boolean requiresPrescription = cartItems.stream()
                .anyMatch(cart -> cart.getMedicine().getPrescriptionRequired());
        
        String orderNumber = "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        
        Order order = Order.builder()
                .orderNumber(orderNumber)
                .user(user)
                .totalAmount(totalAmount)
                .status(requiresPrescription ? Order.OrderStatus.PRESCRIPTION_VERIFICATION : Order.OrderStatus.PENDING)
                .shippingAddress(user.getAddress())
                .shippingCity(user.getCity())
                .shippingState(user.getState())
                .shippingPincode(user.getPincode())
                .contactPhone(user.getPhone())
                .paymentMethod(paymentMethod)
                .paymentStatus("PAID")
                .build();
        
        for (Cart cartItem : cartItems) {
            OrderItem orderItem = OrderItem.builder()
                    .medicine(cartItem.getMedicine())
                    .quantity(cartItem.getQuantity())
                    .price(cartItem.getPrice())
                    .subtotal(cartItem.getSubtotal())
                    .build();
            
            order.addOrderItem(orderItem);
            medicineService.updateStock(cartItem.getMedicine().getId(), cartItem.getQuantity());
        }
        
        Order savedOrder = orderRepository.save(order);
        invoiceService.generateInvoice(savedOrder, paymentMethod);
        cartService.clearCart(user);
        
        return savedOrder;
    }
    
    @Transactional
    public Order createOrder(User user) {
        return createOrderWithPayment(user, "Cash on Delivery");
    }
    
    public Order findById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Order ID cannot be null");
        }
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
    }
    
    public Order findByOrderNumber(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
    }
    
    public List<Order> getUserOrders(User user) {
        return orderRepository.findByUserOrderByOrderDateDesc(user);
    }
    
    public Page<Order> getAllOrders(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return orderRepository.findAllByOrderByOrderDateDesc(pageable);
    }
    
    public List<Order> getRecentOrders(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return orderRepository.findRecentOrders(pageable);
    }
    
    @Transactional
    public Order updateOrderStatus(Long orderId, Order.OrderStatus status) {
        Order order = findById(orderId);
        order.setStatus(status);
        
        if (status == Order.OrderStatus.DELIVERED) {
            order.setDeliveryDate(LocalDateTime.now());
        }
        
        return orderRepository.save(order);
    }
    
    public long getTotalOrders() {
        return orderRepository.count();
    }
    
    public long getTodayOrders() {
        return orderRepository.countTodayOrders();
    }
    
    public long getPendingOrders() {
        return orderRepository.countByStatus(Order.OrderStatus.PENDING);
    }
    
    public long getDeliveredOrders() {
        return orderRepository.countByStatus(Order.OrderStatus.DELIVERED);
    }
    
    public BigDecimal getTotalRevenue() {
        BigDecimal revenue = orderRepository.calculateTotalRevenue();
        return revenue != null ? revenue : BigDecimal.ZERO;
    }
    
    public List<Order> getPharmacistPendingOrders(Long pharmacistId) {
        return orderRepository.findByStatusInAndPharmacistIdIsNull(
            List.of(Order.OrderStatus.PENDING, Order.OrderStatus.PRESCRIPTION_VERIFICATION));
    }
    
    public List<Order> getPharmacistAcceptedOrders(Long pharmacistId) {
        return orderRepository.findByPharmacistIdAndStatusNot(pharmacistId, Order.OrderStatus.REJECTED);
    }
    
    public List<Order> getPharmacistAllOrders(Long pharmacistId) {
        return orderRepository.findByPharmacistIdOrderByOrderDateDesc(pharmacistId);
    }
    
    public Order getOrderById(Long id) {
        return findById(id);
    }
    
    @Transactional
    public void acceptOrderByPharmacist(Long orderId, User pharmacist) {
        Order order = findById(orderId);
        order.setPharmacist(pharmacist);
        order.setStatus(Order.OrderStatus.APPROVED);
        orderRepository.save(order);
    }
    
    @Transactional
    public void rejectOrderByPharmacist(Long orderId, String remarks) {
        Order order = findById(orderId);
        order.setStatus(Order.OrderStatus.REJECTED);
        order.setRemarks(remarks);
        orderRepository.save(order);
    }
    
    @Transactional
    @SuppressWarnings("null")
    public Order createOrderWithPaymentAndPrescription(User user, String paymentMethod, boolean requiresPrescription) {
        List<Cart> cartItems = cartService.getCartItems(user);
        
        if (cartItems.isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }
        
        BigDecimal totalAmount = cartService.getCartTotal(user);
        String orderNumber = "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        
        Order.OrderStatus initialStatus = requiresPrescription ? 
            Order.OrderStatus.PRESCRIPTION_VERIFICATION : Order.OrderStatus.PENDING;
        
        Order order = Order.builder()
                .orderNumber(orderNumber)
                .user(user)
                .totalAmount(totalAmount)
                .status(initialStatus)
                .prescriptionRequired(requiresPrescription)
                .shippingAddress(user.getAddress())
                .shippingCity(user.getCity())
                .shippingState(user.getState())
                .shippingPincode(user.getPincode())
                .contactPhone(user.getPhone())
                .paymentMethod(paymentMethod)
                .paymentStatus("PENDING")
                .build();
        
        for (Cart cartItem : cartItems) {
            OrderItem orderItem = OrderItem.builder()
                    .medicine(cartItem.getMedicine())
                    .quantity(cartItem.getQuantity())
                    .price(cartItem.getPrice())
                    .subtotal(cartItem.getSubtotal())
                    .build();
            
            order.addOrderItem(orderItem);
            medicineService.updateStock(cartItem.getMedicine().getId(), cartItem.getQuantity());
        }
        
        Order savedOrder = orderRepository.save(order);
        
        // Generate invoice immediately for online payments (not for COD)
        if (!"COD".equalsIgnoreCase(paymentMethod) && !"Cash on Delivery".equalsIgnoreCase(paymentMethod)) {
            savedOrder.setPaymentStatus("PAID");
            savedOrder.setPaymentCompletedAt(LocalDateTime.now());
            orderRepository.save(savedOrder);
            invoiceService.generateInvoice(savedOrder, paymentMethod);
        }
        
        cartService.clearCart(user);
        return savedOrder;
    }
    
    @Transactional
    public void completePayment(Long orderId) {
        Order order = findById(orderId);
        order.setPaymentStatus("PAID");
        order.setPaymentCompletedAt(LocalDateTime.now());
        
        Order savedOrder = orderRepository.save(order);
        
        // Generate invoice after payment completion for COD when delivered
        if ("COD".equalsIgnoreCase(order.getPaymentMethod()) || 
            "Cash on Delivery".equalsIgnoreCase(order.getPaymentMethod())) {
            if (order.getStatus() == Order.OrderStatus.DELIVERED) {
                invoiceService.generateInvoice(savedOrder, order.getPaymentMethod());
            }
        }
    }
    
    public boolean checkPrescriptionRequired(User user) {
        List<Cart> cartItems = cartService.getCartItems(user);
        return cartItems.stream()
                .anyMatch(cart -> cart.getMedicine().getPrescriptionRequired());
    }
}
