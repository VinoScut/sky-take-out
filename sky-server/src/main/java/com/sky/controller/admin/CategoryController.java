package com.sky.controller.admin;

import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.CategoryDTO;
import com.sky.entity.Category;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.CategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "分类相关接口")
@RestController
@RequestMapping("/admin/category")
public class CategoryController {

    @Autowired
    CategoryService categoryService;

    /**
     * 分类的分页查询
     * @return
     */
    @ApiOperation("分类分页查询")
    @GetMapping("/page")
    public Result<PageResult<Category>> page(String name, Integer page, Integer pageSize, Integer type) {
        PageResult<Category> pageResult = categoryService.page(name, page, pageSize, type);
        return Result.success(pageResult);
    }

    /**
     * 修改分类信息
     * @return
     */
    @ApiOperation("修改分类信息")
    @PutMapping
    public Result update(@RequestBody CategoryDTO categoryDTO) {
        if(BaseContext.getCurrentId() != 1) {
            return Result.error(MessageConstant.AINT_ADMIN);
        }
        categoryService.update(categoryDTO);
        return Result.success();
    }

    /**
     * 启用或禁用分类
     * @param id
     * @return
     */
    @ApiOperation("启用或禁用分类")
    @PostMapping("/status/{status}")
    public Result enableOrDisable(Long id, @PathVariable Integer status) {
        if(BaseContext.getCurrentId() != 1) {
            return Result.error(MessageConstant.AINT_ADMIN);
        }
        categoryService.enableOrDisable(id, status);
        return Result.success();
    }

    /**
     * 新增分类
     * @return
     */
    @ApiOperation("新增分类")
    @PostMapping
    public Result insert(@RequestBody CategoryDTO categoryDTO) {
        categoryService.insert(categoryDTO);
        return Result.success();
    }

    /**
     * 删除分类
     */
    @ApiOperation("删除分类")
    @DeleteMapping
    public Result delete(Long id) {
        if(BaseContext.getCurrentId() != 1) {
            return Result.error(MessageConstant.AINT_ADMIN);
        }
        categoryService.delete(id);
        return Result.success();
    }

    /**
     * 根据类型查询分类
     */
    @ApiOperation("根据类型查询分类")
    @GetMapping("/list")
    public Result<List<Category>> selectByType(Integer type) {
        List<Category> list = categoryService.selectByType(type);
        return Result.success(list);
    }
}
