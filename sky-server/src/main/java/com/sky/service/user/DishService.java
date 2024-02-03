package com.sky.service.user;


import com.sky.vo.DishVO;

import java.util.List;

public interface DishService {

    List<DishVO> getDishByCategoryId(Long categoryId);
}
