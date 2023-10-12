package com.sky.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.CategoryDTO;
import com.sky.entity.Category;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.CategoryMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetMealMapper;
import com.sky.result.PageResult;
import com.sky.service.CategoryService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    CategoryMapper categoryMapper;

    @Autowired
    DishMapper dishMapper;

    @Autowired
    SetMealMapper setMealMapper;

    LambdaQueryWrapper<Category> lambdaQueryWrapper;

    LambdaUpdateWrapper<Category> lambdaUpdateWrapper;

    @Override
    public PageResult<Category> page(String name, Integer page, Integer pageSize, Integer type) {
        lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.like(StringUtils.isNotEmpty(name), Category::getName, name)
                .eq(type != null, Category::getType, type);
        Page<Category> categoryPage = new Page<>(page, pageSize);
        Page<Category> selectPage = categoryMapper.selectPage(categoryPage, lambdaQueryWrapper);
        return new PageResult<>(selectPage.getTotal(), selectPage.getRecords());
    }

    @Override
    public void update(CategoryDTO categoryDTO) {
        //在传给 DAO 层进行更新时，将 CategoryDTO 转换为 Category，再将此 Category 对象传给 DAO层
        Category category = new Category();
        BeanUtils.copyProperties(categoryDTO, category);
        //设置更新时间以及更新用户
//        category.setUpdateTime(LocalDateTime.now());
//        category.setUpdateUser(BaseContext.getCurrentId());
        categoryMapper.updateById(category);
    }

    @Override
    public void enableOrDisable(Long id, Integer status) {
        lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        Category category = new Category();
        if (id != null && status != null) {
            category.setId(id);
            category.setStatus(status);
        }
        categoryMapper.updateById(category);
    }

    @Override
    public void insert(CategoryDTO categoryDTO) {
        Category category = new Category();
        BeanUtils.copyProperties(categoryDTO, category);
        //设置其他属性值
//        category.setCreateUser(BaseContext.getCurrentId());
//        category.setUpdateUser(BaseContext.getCurrentId());
//        category.setCreateTime(LocalDateTime.now());
//        category.setUpdateTime(LocalDateTime.now());
        //新增的分类，默认是禁用状态，因为新增分类中没有菜品，新增后不应该展示给用户
        category.setStatus(0);
        //在 MybatisPlus 中，添加操作使用的是 .insert()，update() 只负责更新操作
        categoryMapper.insert(category);
    }

    @Override
    public void delete(Long id) {
        //删除分类之前，先去 dish 和 setmeal 表中查看当前分类中是否存在任何菜品或套餐，如果分类不为空，那么不允许删除此分类
        List<Dish> dishes = dishMapper.selectByCategoryId(id);
        if(dishes != null && dishes.size() > 0) {
            throw new DeletionNotAllowedException(MessageConstant.CATEGORY_BE_RELATED_BY_DISH);
        }
        List<Setmeal> setmeals = setMealMapper.selectByCategoryId(id);
        if(setmeals != null && setmeals.size() > 0) {
            throw new DeletionNotAllowedException(MessageConstant.CATEGORY_BE_RELATED_BY_SETMEAL);
        }
        categoryMapper.deleteById(id);
    }

    @Override
    public List<Category> selectByType(Integer type) {
        lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(type != null, Category::getType, type);
        return categoryMapper.selectList(lambdaQueryWrapper);
    }
}
