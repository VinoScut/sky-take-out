package com.sky.controller.admin;

import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.admin.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@Api(tags = "菜品相关接口")
@Slf4j
@RestController
@RequestMapping("/admin/dish")
public class DishController {

    @Autowired
    DishService dishService;
    @Autowired
    RedisTemplate<String, Object> redisTemplate;

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
        //对菜品的修改操作需要删除缓存
        //这里存在一个问题，如果修改了当前菜品的“分类信息”，就会牵扯到两个分类，此时如果要精准删除这两个分类的缓存，就需要去数据库进行查询
        //在分类不多的情况下，可以考虑清除全部缓存
        cleanCache("category_*");
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
        //新增菜品时，由于新增的菜品默认处于 “停售” 状态，所以在新增时无需删除缓存，当新菜品“启售”时，再删除缓存即可
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
        //删除菜品时，由于 “启售” 状态的菜品无法删除，删除的都是 “停售” 的菜品，所以这里也不需要删除缓存
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
        //启售、停售菜品时，需要删除对应的缓存，为了方便，将所有的缓存一并清理掉
        cleanCache("category_*");
        dishService.enableOrDisable(id, status);
        return Result.success();
    }

    @GetMapping("/list")
    @ApiOperation("根据分类id查询菜品")
    public Result<List<Dish>> selectDishByCategoryId(Long categoryId) {
        List<Dish> dishList = dishService.selectDishByCategoryId(categoryId);
        return Result.success(dishList);
    }


    /**
     * 根据 pattern 删除 redis 缓存，保持数据一致性
     * @param pattern 样式
     */
    private void cleanCache(String pattern) {
        Set<String> keys = redisTemplate.keys(pattern);
        if(keys != null) {
            redisTemplate.delete(keys);
        }
    }

}
