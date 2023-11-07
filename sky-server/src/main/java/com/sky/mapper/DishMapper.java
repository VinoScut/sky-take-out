package com.sky.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.vo.DishVO;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DishMapper extends BaseMapper<Dish> {

    @Select("select * from dish where category_id = #{categoryId}")
    List<Dish> selectByCategoryId(Long categoryId);

    Page<DishVO> selectPage(Page<DishVO> dishVOPage, DishPageQueryDTO dishPageQueryDTO);

    DishVO selectById(Integer id);

    @Select("select status from dish where id = #{id}")
    Integer checkStatus(Long id);

}
