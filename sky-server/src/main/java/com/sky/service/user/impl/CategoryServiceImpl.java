package com.sky.service.user.impl;

import com.sky.entity.Category;
import com.sky.mapper.user.CategoryMapper;
import com.sky.service.user.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service("userCategoryService")
public class CategoryServiceImpl implements CategoryService {
    @Autowired
    CategoryMapper categoryMapper;


    @Override
    public List<Category> getCategory(Integer type) {
        return categoryMapper.getCategory(type);
    }
}
