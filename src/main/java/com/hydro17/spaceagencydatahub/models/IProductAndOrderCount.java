package com.hydro17.spaceagencydatahub.models;

public interface IProductAndOrderCount {
    Product getProduct();

    //This getter return the product order count
    Long getOrderCount();
}
