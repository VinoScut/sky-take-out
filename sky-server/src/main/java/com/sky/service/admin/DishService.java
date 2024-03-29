package com.sky.service.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.vo.DishVO;

import java.util.List;


public interface DishService {

    PageResult<DishVO> pageQuery(DishPageQueryDTO dishPageQueryDTO);

    DishVO selectById(Integer id);

    void add(DishDTO dishDTO);

    int delete(String ids);

    void enableOrDisable(Long id, Integer status);

    void edit(DishDTO dishDTO);

    List<Dish> selectDishByCategoryId(Long categoryId);
}
