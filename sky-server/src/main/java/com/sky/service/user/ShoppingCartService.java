package com.sky.service.user;

import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;

import java.util.List;

public interface ShoppingCartService {

    void add(ShoppingCartDTO shoppingCartDTO);

    List<ShoppingCart> checkShoppingCart();

    void sub(ShoppingCartDTO shoppingCartDTO);

    void cleanShoppingCart();
}
