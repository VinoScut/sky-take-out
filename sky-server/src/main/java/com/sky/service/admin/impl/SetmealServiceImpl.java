package com.sky.service.admin.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.mapper.admin.SetMealDishMapper;
import com.sky.mapper.admin.SetMealMapper;
import com.sky.result.PageResult;
import com.sky.service.admin.SetmealService;
import com.sky.vo.SetmealVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service("adminSetmealService")
public class SetmealServiceImpl implements SetmealService {
    @Autowired
    SetMealMapper setMealMapper;
    @Autowired
    SetMealDishMapper setMealDishMapper;

    @Override
    public SetmealVO getSetmealById(Long id) {
        return setMealMapper.getSetmealById(id);
    }

    @Override
    public void add(SetmealDTO setmealDTO) {
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        setMealMapper.insert(setmeal);
        setmealDTO.getSetmealDishes().forEach(setmealDish -> {
            setmealDish.setSetmealId(setmeal.getId());
            setMealDishMapper.insert(setmealDish);
        });
    }

    @Override
    public void batchDelete(String ids) {
        //先判断传入的参数是否为 null 或者不存在任何有效id
        if(StringUtils.hasText(ids)) {
            // setmeal 中的 id 与 setmealDish 的 setmeal_id 构成外键约束，先删除从表 setmealDish 中的数据，再删除主表的数据
            setMealDishMapper.batchDeleteBySetmealId(ids);
            setMealMapper.batchDelete(ids);
        }
    }

    @Override
    public void enableOrDisable(Long id, Integer status) {
        LambdaUpdateWrapper<Setmeal> setmealLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        setmealLambdaUpdateWrapper.set(status != null, Setmeal::getStatus, status)
                                    .eq(id != null, Setmeal::getId, id);
        setMealMapper.update(null, setmealLambdaUpdateWrapper);
    }

    @Override
    public PageResult<Setmeal> pageQuery(SetmealPageQueryDTO setmealPageQueryDTO) {
        Page<Setmeal> setmealPage = new Page<>(setmealPageQueryDTO.getPage(), setmealPageQueryDTO.getPageSize());
        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper = new LambdaQueryWrapper<>();
        Integer categoryId = setmealPageQueryDTO.getCategoryId();
        Integer status = setmealPageQueryDTO.getStatus();
        String name = setmealPageQueryDTO.getName();
        setmealLambdaQueryWrapper.eq(categoryId != null, Setmeal::getCategoryId, categoryId)
                                    .eq(status != null, Setmeal::getStatus, status)
                                    .like(name != null, Setmeal::getName, name);
        setmealPage = setMealMapper.selectPage(setmealPage, setmealLambdaQueryWrapper);
        return new PageResult<>(setmealPage.getTotal(), setmealPage.getRecords());
    }

    @Override
    public void edit(SetmealDTO setmealDTO) {
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        setMealMapper.updateById(setmeal);
        List<SetmealDish> newSetmealDishes = setmealDTO.getSetmealDishes();
        setMealDishMapper.deleteBySetmealId(setmealDTO.getId());
        newSetmealDishes.forEach(setmealDish -> {
            setmealDish.setSetmealId(setmealDTO.getId());
            setMealDishMapper.insert(setmealDish);
        });
    }
}
