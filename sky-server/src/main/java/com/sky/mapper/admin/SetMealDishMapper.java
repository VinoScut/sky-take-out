package com.sky.mapper.admin;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sky.entity.SetmealDish;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SetMealDishMapper extends BaseMapper<SetmealDish> {

    @Select("select id from setmeal_dish where dish_id = #{dishId}")
    SetmealDish selectByDishId(Long dishId);

    @Select("select * from setmeal_dish where setmeal_id = #{setmealId}")
    List<SetmealDish> selectBySetmealId(Long setmealId);

    @Delete("delete from setmeal_dish where setmeal_id = #{setmealId}")
    void deleteBySetmealId(Long setmealId);

    @Delete("delete from setmeal_dish where setmeal_id in (${setmealIds})")
    void batchDeleteBySetmealId(String setmealIds);
}
