package com.sky.mapper.user;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sky.entity.Setmeal;
import com.sky.vo.DishItemVO;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("userSetmealMapper")
public interface SetmealMapper extends BaseMapper<Setmeal> {

    @Select("select * from setmeal where category_id = #{categoryId} and status = 1")
    List<Setmeal> getSetmealByCategoryId(Long categoryId);

    List<DishItemVO> getDishItemBySetmealId(Long setmealId);

}
