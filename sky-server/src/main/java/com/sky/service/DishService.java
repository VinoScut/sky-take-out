package com.sky.service;

import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.vo.DishVO;


public interface DishService {

    PageResult<DishVO> pageQuery(DishPageQueryDTO dishPageQueryDTO);

    DishVO selectById(Integer id);
}
