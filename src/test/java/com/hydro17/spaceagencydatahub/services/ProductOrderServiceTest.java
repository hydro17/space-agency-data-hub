package com.hydro17.spaceagencydatahub.services;

import com.hydro17.spaceagencydatahub.models.*;
import com.hydro17.spaceagencydatahub.repositories.ProductOrderRepository;
import com.hydro17.spaceagencydatahub.utils.ImageryType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = ProductOrderService.class)
class ProductOrderServiceTest {

    @Autowired
    ProductOrderService productOrderService;

    @MockBean
    ProductOrderRepository productOrderRepository;

    @MockBean
    ProductService productService;

    private List<ProductOrder> nonEmptyProductOrderList;
    private List<ProductOrder> emptyProductOrderList;

    private ProductOrder nonEmptyProductOrder;
    private ProductOrderDTO nonEmptyProductOrderDTO;
    private Product product;

    @BeforeEach
    void setUp() {
        Mission mission = new Mission();
        mission.setId(1L);
        mission.setName("mission");
        mission.setImageryType(ImageryType.HYPERSPECTRAL);
        mission.setStartDate(LocalDateTime.now());
        mission.setFinishDate(LocalDateTime.now().plusHours(1L));

        ProductFootprint footprint = new ProductFootprint();
        footprint.setStartCoordinateLatitude(100.15);
        footprint.setEndCoordinateLatitude(200.99);
        footprint.setStartCoordinateLongitude(10.5);
        footprint.setEndCoordinateLongitude(50.7);

        product = new Product();
        product.setId(1L);
        product.setAcquisitionDate(LocalDateTime.now());
        product.setFootprint(footprint);
        product.setPrice(new BigDecimal("10.5"));
        product.setUrl("http://com1");
        product.setMission(mission);
        mission.addProduct(product);

        nonEmptyProductOrder = new ProductOrder();
        nonEmptyProductOrder.setId(1L);
        nonEmptyProductOrder.setPlacedOn(LocalDateTime.now());
        nonEmptyProductOrder.addProduct(product);

        nonEmptyProductOrderList = new ArrayList<>();
        nonEmptyProductOrderList.add(nonEmptyProductOrder);

        emptyProductOrderList = new ArrayList<>();

        nonEmptyProductOrderDTO = new ProductOrderDTO();
        nonEmptyProductOrderDTO.setProductIds(Arrays.asList(1L, 2L, 3L));
    }

    @Test
    void getAllProductOrdersOrderedByPlacedOnDesc_whenValidInput_thenReturnsNonEmptyProductOrderList() {
        when(productOrderRepository.findAllOrdersOrderedByPlacedOnDesc()).thenReturn(nonEmptyProductOrderList);

        List<ProductOrder> actualOutput = productOrderService.getAllProductOrdersOrderedByPlacedOnDesc();

        assertThat(actualOutput).isEqualTo(nonEmptyProductOrderList);
    }

    @Test
    void saveProductOrder_whenValidInput_thenReturnsProductOrder() {
        when(productOrderRepository.save(nonEmptyProductOrder)).thenReturn(nonEmptyProductOrder);

        ProductOrder actualOutput = productOrderService.saveProductOrder(nonEmptyProductOrder);

        assertThat(actualOutput).isEqualTo(nonEmptyProductOrder);
    }

    @Test
    void isOrderedProductById_whenProductIsOrdered_thenReturnsTrue() {
        when(productOrderRepository.findAllProductOrdersContainingProductWithGivenId(anyLong())).thenReturn(nonEmptyProductOrderList);

        boolean actualOutput = productOrderService.isOrderedProductById(1L);

        assertThat(actualOutput).isTrue();
    }

    @Test
    void isOrderedProductById_whenProductIsNotOrdered_thenReturnsFalse() {
        when(productOrderRepository.findAllProductOrdersContainingProductWithGivenId(anyLong())).thenReturn(emptyProductOrderList);

        boolean actualOutput = productOrderService.isOrderedProductById(1L);

        assertThat(actualOutput).isFalse();
    }

    @Test
    void convertProductOrderDTOToProductOrder_whenValidInput_thenReturnsProductOrder() {
        when(productService.getProductById(anyLong())).thenReturn(java.util.Optional.ofNullable(product));

        ProductOrder actualOutput = productOrderService.convertProductOrderDTOToProductOrder(nonEmptyProductOrderDTO);

        assertThat(actualOutput.getOrderItems().get(0).getProduct()).isEqualTo(product);
    }
}