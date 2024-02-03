package com.sky.controller.admin;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.admin.SetmealService;
import com.sky.vo.SetmealVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.web.bind.annotation.*;


@RestController("adminSetmealController")
@RequestMapping("/admin/setmeal")
@Api(tags = "套餐相关接口")
public class SetmealController {
    @Autowired
    SetmealService setmealService;

    @GetMapping("/{id}")
    @ApiOperation("根据id查询套餐")
    public Result<SetmealVO> getSetmealById(@PathVariable("id") Long id) {
        SetmealVO setmealVO = setmealService.getSetmealById(id);
        return Result.success(setmealVO);
    }

    @PostMapping
    @ApiOperation("新增套餐")
    @CacheEvict(cacheNames = "category", key = "#setmealDTO.categoryId")
    public Result addSetmeal(@RequestBody SetmealDTO setmealDTO) {
        setmealService.add(setmealDTO);
        return Result.success();
    }

    @DeleteMapping
    @ApiOperation("批量删除套餐")
    @CacheEvict(cacheNames = "category", allEntries = true)
    public Result batchDelete(@RequestParam String ids) {
        setmealService.batchDelete(ids);
        return Result.success();
    }

    @PostMapping("/status/{status}")
    @ApiOperation("套餐启售、停售")
    @CacheEvict(cacheNames = "category", allEntries = true)
    public Result enableOrDisable(@RequestParam Long id, @PathVariable Integer status) {
        setmealService.enableOrDisable(id, status);
        return Result.success();
    }

    @PutMapping
    @ApiOperation("修改套餐")
    @CacheEvict(cacheNames = "category", allEntries = true)
    public Result edit(@RequestBody SetmealDTO setmealDTO) {
        setmealService.edit(setmealDTO);
        return Result.success();
    }

    @GetMapping("/page")
    @ApiOperation("分页查询")
    public Result<PageResult<Setmeal>> pageQuery(SetmealPageQueryDTO setmealPageQueryDTO) {
        PageResult<Setmeal> pageResult = setmealService.pageQuery(setmealPageQueryDTO);
        return Result.success(pageResult);
    }


}
