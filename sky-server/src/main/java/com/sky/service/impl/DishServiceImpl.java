package com.sky.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sky.dto.DishPageQueryDTO;
import com.sky.mapper.CategoryMapper;
import com.sky.mapper.DishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DishServiceImpl implements DishService {
    @Autowired
    DishMapper dishMapper;

    @Autowired
    CategoryMapper categoryMapper;

    @Override
    public PageResult<DishVO> pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        Page<DishVO> dishVOPage = new Page<>(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());
        dishVOPage = dishMapper.selectPage(dishVOPage, dishPageQueryDTO);
        return new PageResult<>(dishVOPage.getTotal(), dishVOPage.getRecords());
    }

    @Override
    public DishVO selectById(Integer id) {
        DishVO dishVO = dishMapper.selectById(id);
        dishVO.setCategoryName(categoryMapper.getCategoryNameByDishId(dishVO.getCategoryId()));
        return dishVO;
    }
}
