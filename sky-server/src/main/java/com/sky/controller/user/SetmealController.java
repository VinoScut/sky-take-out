package com.sky.controller.user;

import com.sky.entity.Setmeal;
import com.sky.result.Result;
import com.sky.service.user.SetmealService;
import com.sky.vo.DishItemVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController("userSetmealController")
@RequestMapping("/user/setmeal")
@Api(tags = "C端套餐接口")
public class SetmealController {
    @Autowired
    SetmealService setmealService;

    @GetMapping("/list")
    @Cacheable(cacheNames = "category", key = "#categoryId")
    @ApiOperation("通过分类id获取套餐")
    public Result<List<Setmeal>> getSetmealByCategoryId(@RequestParam Long categoryId) {
        List<Setmeal> setmealList = setmealService.getSetmealByCategoryId(categoryId);
        return Result.success(setmealList);
    }

    @GetMapping("/dish/{id}")
    @ApiOperation("根据套餐id查询其中的菜品")
    public Result<List<DishItemVO>> getDishItemBySetmealId(@PathVariable("id") Long setmealId) {
        List<DishItemVO> dishItemVOList = setmealService.getDishItemBySetmealId(setmealId);
        return Result.success(dishItemVOList);
    }

}
