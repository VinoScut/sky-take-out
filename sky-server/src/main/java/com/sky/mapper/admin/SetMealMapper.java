package com.sky.mapper.admin;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sky.entity.Setmeal;
import com.sky.vo.SetmealVO;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository("adminSetmealMapper")
public interface SetMealMapper extends BaseMapper<Setmeal> {

    List<Setmeal> selectByCategoryId(Long categoryId);

    SetmealVO getSetmealById(Long id);

    void batchDelete(String ids);

    @Select("select id, name, status from setmeal")
    List<Setmeal> getSetmealList();
}
