package com.sky.service.admin.impl;

import com.sky.service.admin.ShopService;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class ShopServiceImpl implements ShopService {
    private static final String SHOP_STATUS_KEY = "SHOP_STATUS";

    @Resource(name = "redisTemplate")
    ValueOperations<String, Integer> valueOperations;

    @Override
    public void editShopStatus(Integer status) {
        valueOperations.set(SHOP_STATUS_KEY, status);
    }

    @Override
    public Integer getShopStatus() {
        return valueOperations.get(SHOP_STATUS_KEY);
    }
}
