package com.sky.mapper.admin;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sky.entity.DishFlavor;
import org.apache.ibatis.annotations.MapKey;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface DishFlavorMapper extends BaseMapper<DishFlavor> {

    DishFlavor selectByDishId();

    void deleteByDishIds(List<Long> dishIds);

    @MapKey("name")
    Map<String, DishFlavor> getDishFlavorByDishId(Long dishId);

    void update(DishFlavor dishFlavor);

    void deleteByDishIdAndName(DishFlavor dishFlavor);

    void updateByDishIdAndName(DishFlavor dishFlavor);

//    List<DishFlavor> getDishFlavorByDishId(Long dishId);
}
