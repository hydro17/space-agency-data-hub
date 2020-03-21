package com.hydro17.spaceagencydatahub.repositories;

import com.hydro17.spaceagencydatahub.models.ProductAndOrderCount;
import com.hydro17.spaceagencydatahub.models.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    @Query("SELECT new com.hydro17.spaceagencydatahub.models.ProductAndOrderCount(p, COUNT(oi)) FROM OrderItem oi JOIN oi.product p " +
            "GROUP BY p.id ORDER BY COUNT(oi) DESC")
    List<ProductAndOrderCount> findAllProductAndOrderCountGroupedByProductIdOrderedByOrderCountDesc();

//    @Query("SELECT p, COUNT(oi) as order_count FROM OrderItem oi JOIN oi.product p " +
//            "GROUP BY p.id ORDER BY order_count DESC")
//    List<Object[]> findAllProductAndOrderCountGroupedByProductIdOrderedByOrderCountDesc();
}
