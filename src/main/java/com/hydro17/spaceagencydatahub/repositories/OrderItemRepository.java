package com.hydro17.spaceagencydatahub.repositories;

import com.hydro17.spaceagencydatahub.models.IProductAndOrderCount;
import com.hydro17.spaceagencydatahub.models.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    @Query("SELECT p as product, COUNT(oi) as orderCount FROM OrderItem oi JOIN oi.product p " +
            "GROUP BY p.id ORDER BY orderCount DESC")
    List<IProductAndOrderCount> findAllProductAndOrderCountGroupedByProductIdOrderedByOrderCountDesc();
}
