package com.sky.controller.user;

import com.sky.entity.Category;
import com.sky.result.Result;
import com.sky.service.user.CategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController("userCategoryController")
@RequestMapping("/user/category")
@Api(tags = "C端分类接口")
public class CategoryController {
    @Autowired
    CategoryService categoryService;


    @GetMapping("/list")
    @ApiOperation("获取分类")
    public Result<List<Category>> getCategory(@RequestParam(required = false) Integer type) {
        return Result.success(categoryService.getCategory(type));
    }
}
