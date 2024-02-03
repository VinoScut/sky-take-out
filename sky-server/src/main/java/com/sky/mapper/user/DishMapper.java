package com.sky.mapper.user;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sky.entity.Dish;
import com.sky.vo.DishVO;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("userDishMapper")
public interface DishMapper extends BaseMapper<Dish> {

    List<DishVO> getDishByCategoryId(Long categoryId);

}
