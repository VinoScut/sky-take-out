package com.sky.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.mapper.CategoryMapper;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetMealDishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DishServiceImpl implements DishService {
    @Autowired
    DishMapper dishMapper;

    @Autowired
    CategoryMapper categoryMapper;

    @Autowired
    DishFlavorMapper dishFlavorMapper;

    @Autowired
    SetMealDishMapper setMealDishMapper;

    @Override
    public PageResult<DishVO> pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        Page<DishVO> dishVOPage = new Page<>(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());
        dishVOPage = dishMapper.selectPage(dishVOPage, dishPageQueryDTO);
        return new PageResult<>(dishVOPage.getTotal(), dishVOPage.getRecords());
    }

    @Override
    public DishVO selectById(Integer id) {
        DishVO dishVO = dishMapper.selectById(id);
        dishVO.setCategoryName(categoryMapper.getCategoryNameByDishId(dishVO.getCategoryId()));
        return dishVO;
    }

    @Override
    @Transactional
    public void add(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        //先完成 dish 的新增操作，获取到当前这个 dish 的自增主键，这样在下面才能为 dishFlavor 的 dishId 进行赋值
        dishMapper.insert(dish);
        List<DishFlavor> flavors = dishDTO.getFlavors();
        flavors.forEach(dishFlavor -> {
            dishFlavor.setDishId(dish.getId());
            dishFlavorMapper.insert(dishFlavor);
        });
    }

    @Override
    @Transactional
    public int delete(String ids) {
        //(1)检查是否存在 “启售状态” 的菜品，正在销售的菜品不能删除
        //(2)检查当前菜品是否关联在某个 “套餐” 中
        //(3)排除掉以上两种情况的菜品后，剩余的菜品可以删除，但要先删除引用了当前菜品的主键的 dishFlavor从表 中的数据，然后再删除菜品
        List<Long> dishToDelete = Arrays.stream(ids.split(","))
                                        .map(Long::parseLong)
                                        .filter(id -> dishMapper.checkStatus(id) == 0)
                                        .filter(id -> setMealDishMapper.selectByDishId(id) == null)
                                        .collect(Collectors.toList());
        //仅当要删除的菜品数量大于1时，才进行删除操作
        //之所以进行一次判断，是因为在 mp 中，wrapper 和 deleteBatchIds 都要求列表不为空，否则会出错
        if(dishToDelete.size() > 0) {
            //先删除 从表dishFlavor 中的数据
//            LambdaQueryWrapper<DishFlavor> dishFlavorLambdaQueryWrapper = new LambdaQueryWrapper<>();
//            dishFlavorLambdaQueryWrapper.in(DishFlavor::getDishId, dishToDelete);
//            dishFlavorMapper.delete(dishFlavorLambdaQueryWrapper);
            dishFlavorMapper.deleteByDishIds(dishToDelete);
            //再删除 主表dish 中的数据
            dishMapper.deleteBatchIds(dishToDelete);
        }
        return dishToDelete.size();
    }

    @Override
    public void enableOrDisable(Long id, Integer status) {
        Dish dish = new Dish();
        dish.setId(id);
        dish.setStatus(status);
        dishMapper.updateById(dish);
    }
}
