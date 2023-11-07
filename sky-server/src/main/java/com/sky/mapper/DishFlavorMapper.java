package com.sky.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sky.entity.DishFlavor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DishFlavorMapper extends BaseMapper<DishFlavor> {

    DishFlavor selectByDishId();

    void deleteByDishIds(List<Long> dishIds);
}
