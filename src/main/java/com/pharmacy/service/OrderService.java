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
    private final NotificationService notificationService;
    
    public OrderService(OrderRepository orderRepository, CartService cartService, 
                        MedicineService medicineService, InvoiceService invoiceService,
                        NotificationService notificationService) {
        this.orderRepository = orderRepository;
        this.cartService = cartService;
        this.medicineService = medicineService;
        this.invoiceService = invoiceService;
        this.notificationService = notificationService;
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
            
            // Generate invoice for COD orders when delivered
            if (("COD".equalsIgnoreCase(order.getPaymentMethod()) || 
                 "Cash on Delivery".equalsIgnoreCase(order.getPaymentMethod()))) {
                // Check if invoice already exists
                Invoice existingInvoice = invoiceService.findByOrderId(order.getId());
                if (existingInvoice == null) {
                    invoiceService.generateInvoice(order, order.getPaymentMethod());
                    
                    // Notify customer about invoice generation for COD
                    notificationService.createNotification(
                            order.getUser(),
                            Notification.NotificationType.ORDER_STATUS_UPDATE,
                            "Invoice Generated",
                            String.format("Invoice has been generated for your delivered order #%s. You can download it from your order history.",
                                    order.getOrderNumber()),
                            "Order",
                            order.getId()
                    );
                }
            }
            
            // Notify customer about delivery
            notificationService.createNotification(
                    order.getUser(),
                    Notification.NotificationType.ORDER_DELIVERED,
                    "Order Delivered",
                    String.format("Your order #%s has been delivered successfully. Please provide feedback!",
                            order.getOrderNumber()),
                    "Order",
                    order.getId()
            );
        } else if (status == Order.OrderStatus.SHIPPED) {
            // Notify customer when shipped
            notificationService.createNotification(
                    order.getUser(),
                    Notification.NotificationType.ORDER_STATUS_UPDATE,
                    "Order Shipped",
                    String.format("Your order #%s has been shipped and is on the way.",
                            order.getOrderNumber()),
                    "Order",
                    order.getId()
            );
        } else if (status == Order.OrderStatus.PACKED) {
            // Notify customer when packed
            notificationService.createNotification(
                    order.getUser(),
                    Notification.NotificationType.ORDER_STATUS_UPDATE,
                    "Order Packed",
                    String.format("Your order #%s has been packed and will be dispatched soon.",
                            order.getOrderNumber()),
                    "Order",
                    order.getId()
            );
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
        
        // Notify customer about order acceptance
        notificationService.createNotification(
                order.getUser(),
                Notification.NotificationType.ORDER_STATUS_UPDATE,
                "Order Approved",
                String.format("Your order #%s has been approved by pharmacist %s and is being prepared.",
                        order.getOrderNumber(), pharmacist.getFullName()),
                "Order",
                order.getId()
        );
        
        // Notify pharmacist
        notificationService.createNotification(
                pharmacist,
                Notification.NotificationType.ORDER_STATUS_UPDATE,
                "Order Assigned",
                String.format("Order #%s has been assigned to you.", order.getOrderNumber()),
                "Order",
                order.getId()
        );
    }
    
    @Transactional
    public void rejectOrderByPharmacist(Long orderId, String remarks) {
        Order order = findById(orderId);
        order.setStatus(Order.OrderStatus.REJECTED);
        order.setRemarks(remarks);
        orderRepository.save(order);
        
        // Notify customer about order rejection
        notificationService.createNotification(
                order.getUser(),
                Notification.NotificationType.ORDER_STATUS_UPDATE,
                "Order Rejected",
                String.format("Your order #%s has been rejected. Reason: %s",
                        order.getOrderNumber(), remarks != null ? remarks : "Not specified"),
                "Order",
                order.getId()
        );
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
            
            // Notify customer about successful payment and invoice
            notificationService.createNotification(
                    user,
                    Notification.NotificationType.PAYMENT_SUCCESS,
                    "Payment Successful",
                    String.format("Payment for order #%s completed successfully. Invoice generated.", orderNumber),
                    "Order",
                    savedOrder.getId()
            );
        } else {
            // Notify customer about COD order placement
            notificationService.createNotification(
                    user,
                    Notification.NotificationType.ORDER_STATUS_UPDATE,
                    "Order Placed",
                    String.format("Your order #%s has been placed successfully. Pay on delivery.", orderNumber),
                    "Order",
                    savedOrder.getId()
            );
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
                // Check if invoice already exists
                Invoice existingInvoice = invoiceService.findByOrderId(order.getId());
                if (existingInvoice == null) {
                    invoiceService.generateInvoice(savedOrder, order.getPaymentMethod());
                }
            }
        }
    }
    
    public boolean checkPrescriptionRequired(User user) {
        List<Cart> cartItems = cartService.getCartItems(user);
        return cartItems.stream()
                .anyMatch(cart -> cart.getMedicine().getPrescriptionRequired());
    }
}
