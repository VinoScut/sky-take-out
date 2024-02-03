package com.sky.service.user;

import com.sky.entity.Setmeal;
import com.sky.vo.DishItemVO;

import java.util.List;

public interface SetmealService {

    List<Setmeal> getSetmealByCategoryId(Long categoryId);

    List<DishItemVO> getDishItemBySetmealId(Long setmealId);
}
