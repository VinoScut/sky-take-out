package com.sky.service.user.impl;

import com.sky.entity.Setmeal;
import com.sky.mapper.user.SetmealMapper;
import com.sky.service.user.SetmealService;
import com.sky.vo.DishItemVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("userSetmealService")
public class SetmealServiceImpl implements SetmealService  {
    @Autowired
    SetmealMapper setmealMapper;

    @Override
    public List<Setmeal> getSetmealByCategoryId(Long categoryId) {
        return setmealMapper.getSetmealByCategoryId(categoryId);
    }

    @Override
    public List<DishItemVO> getDishItemBySetmealId(Long setmealId) {
        return setmealMapper.getDishItemBySetmealId(setmealId);
    }
}
