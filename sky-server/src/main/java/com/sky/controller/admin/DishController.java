package com.sky.controller.admin;

import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.admin.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Api(tags = "菜品相关接口")
@Slf4j
@RestController
@RequestMapping("/admin/dish")
public class DishController {

    @Autowired
    DishService dishService;

    /**
     * 菜品分页查询
     * @param dishPageQueryDTO
     * @return
     */
    @ApiOperation("菜品分页查询")
    @GetMapping("/page")
    public Result<PageResult<DishVO>> pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        PageResult<DishVO> pageResult = dishService.pageQuery(dishPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 根据id查询菜品
     * @param id
     * @return
     */
    @ApiOperation("根据id查询菜品")
    @GetMapping("/{id}")
    public Result<DishVO> selectById(@PathVariable Integer id) {
        DishVO dishVo = dishService.selectById(id);
        return Result.success(dishVo);
    }

    /**
     * 修改菜品
     * @return
     */
    @ApiOperation("修改菜品")
    @PutMapping
    public Result edit(@RequestBody DishDTO dishDTO) {
        dishService.edit(dishDTO);
        return Result.success();
    }

    /**
     * 新增菜品
     * @param dishDTO
     * @return
     */
    @ApiOperation("新增菜品")
    @PostMapping
    public Result add(@RequestBody DishDTO dishDTO) {
        dishService.add(dishDTO);
        return Result.success();
    }

    /**
     * 删除菜品
     * @param ids
     * @return
     */
    @ApiOperation("删除菜品")
    @DeleteMapping
    public Result delete(String ids) {
        if(BaseContext.getCurrentId() != 1) {
            return Result.error(MessageConstant.AINT_ADMIN);
        }
        //拿到实际删除的菜品数量，分类讨论，决定返回给前端的提示信息
        int deleteNum = dishService.delete(ids);
        if(deleteNum == 0) {
            return Result.error("所选菜品全部为启售状态或关联在套餐中，删除失败");
        }
        if(deleteNum < ids.split(",").length) {
            return Result.error("仅删除了所选的部分菜品，其余菜品为启售状态或关联在套餐中，无法删除");
        }
        return Result.success("所选菜品已全部删除");
    }

    /**
     * 起售、停售菜品
     * @param id
     * @param status
     * @return
     */
    @ApiOperation("起售、停售菜品")
    @PostMapping("/status/{status}")
    public Result enableOrDisable(Long id, @PathVariable Integer status) {
        dishService.enableOrDisable(id, status);
        return Result.success();
    }

}
