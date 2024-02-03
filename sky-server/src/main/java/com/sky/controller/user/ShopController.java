package com.sky.controller.user;

import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController("userShopController")
@RequestMapping("/user/shop")
@Api(tags = "C端店铺接口")
@Slf4j
public class ShopController {
    private static final String SHOP_STATUS_KEY = "SHOP_STATUS";

    @Resource(name = "redisTemplate")
    ValueOperations<String, Integer> valueOperations;

    /**
     * 查询店铺状态
     * @return
     */
    @ApiOperation("查询店铺状态")
    @GetMapping("/status")
    public Result<Integer> getShopStatus() {
        return Result.success(valueOperations.get(SHOP_STATUS_KEY));
    }
}
