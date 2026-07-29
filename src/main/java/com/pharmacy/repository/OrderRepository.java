package com.pharmacy.repository;

import com.pharmacy.entity.Order;
import com.pharmacy.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderNumber(String orderNumber);
    
    List<Order> findByUserOrderByOrderDateDesc(User user);
    
    Page<Order> findAllByOrderByOrderDateDesc(Pageable pageable);
    
    @Query("SELECT o FROM Order o WHERE o.status = :status ORDER BY o.orderDate DESC")
    List<Order> findByStatus(@Param("status") Order.OrderStatus status);
    
    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.status NOT IN ('CANCELLED')")
    BigDecimal calculateTotalRevenue();
    
    @Query("SELECT o FROM Order o ORDER BY o.orderDate DESC")
    List<Order> findRecentOrders(Pageable pageable);
    
    @Query("SELECT COUNT(o) FROM Order o WHERE FUNCTION('DATE', o.orderDate) = CURRENT_DATE")
    long countTodayOrders();
    
    long countByStatus(Order.OrderStatus status);
}
