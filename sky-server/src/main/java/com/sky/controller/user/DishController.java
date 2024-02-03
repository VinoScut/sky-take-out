package com.sky.controller.user;

import com.sky.result.Result;
import com.sky.service.user.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController("userDishController")
@RequestMapping("/user/dish")
@Api(tags = "C端菜品接口")
public class DishController {
    @Autowired
    DishService dishService;

    @GetMapping("/list")
    @ApiOperation("由分类id获取菜品")
    public Result<List<DishVO>> getDishByCategoryId(@RequestParam Long categoryId) {
        List<DishVO> dishVOList = dishService.getDishByCategoryId(categoryId);
        return Result.success(dishVOList);
    }
}
