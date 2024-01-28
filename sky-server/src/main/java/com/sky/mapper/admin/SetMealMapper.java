package com.sky.mapper.admin;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sky.entity.Setmeal;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SetMealMapper extends BaseMapper<Setmeal> {

    List<Setmeal> selectByCategoryId(Long categoryId);
}
