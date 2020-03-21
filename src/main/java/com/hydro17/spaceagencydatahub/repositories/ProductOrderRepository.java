package com.hydro17.spaceagencydatahub.repositories;

import com.hydro17.spaceagencydatahub.models.Product;
import com.hydro17.spaceagencydatahub.models.ProductOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductOrderRepository extends JpaRepository<ProductOrder, Long> {

    @Query("SELECT po FROM ProductOrder po ORDER BY po.placedOn DESC")
    List<ProductOrder> findAllOrderByPlacedOnDesc();

    @Query("SELECT po FROM ProductOrder po JOIN FETCH po.orderItems oi JOIN FETCH oi.product p WHERE p.id = :id")
    List<ProductOrder> findAllProductOrdersContainingProductWithGivenId(@Param("id") long id);
}
