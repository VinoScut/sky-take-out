package com.sky.controller.admin;

import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.result.Result;
import com.sky.service.admin.ShopService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController("adminShopController")
@RequestMapping("/admin/shop")
@Api(tags = "店铺相关接口")
@Slf4j
public class ShopController {

    @Autowired
    ShopService shopService;

    /**
     * 设置店铺状态
     * @param status
     * @return
     */
    @ApiOperation("设置店铺状态")
    @PutMapping("/{status}")
    public Result editShopStatus(@PathVariable Integer status) {
        if(BaseContext.getCurrentId() != 1) {
            return Result.error(MessageConstant.AINT_ADMIN);
        }
        //记录日志，便于状态追踪和后续维护
        log.info("修改店铺的营业状态，当前营业状态为：{}", status == 1 ? "营业中" : "打烊中");
        shopService.editShopStatus(status);
        return Result.success();
    }

    /**
     * 查询店铺状态
     * @return
     */
    @ApiOperation("查询店铺状态")
    @GetMapping("/status")
    public Result<Integer> getShopStatus() {
        return Result.success(shopService.getShopStatus());
    }
}
