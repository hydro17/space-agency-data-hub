package com.hydro17.spaceagencydatahub.repositories;

import com.hydro17.spaceagencydatahub.models.ProductOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductOrderRepository extends JpaRepository<ProductOrder, Long> {

    @Query("SELECT DISTINCT po FROM ProductOrder po LEFT JOIN FETCH po.orderItems ORDER BY po.placedOn DESC")
    List<ProductOrder> findAllOrdersOrderedByPlacedOnDesc();

    @Query("SELECT po FROM ProductOrder po JOIN po.orderItems oi JOIN oi.product p WHERE p.id = :id")
    List<ProductOrder> findAllProductOrdersContainingProductWithGivenId(@Param("id") long id);
}
