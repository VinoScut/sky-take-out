package com.sky.service.admin;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.result.PageResult;
import com.sky.vo.SetmealVO;

public interface SetmealService {
    SetmealVO getSetmealById(Long id);

    void add(SetmealDTO setmealDTO);

    void batchDelete(String ids);

    void enableOrDisable(Long id, Integer status);

    PageResult<Setmeal> pageQuery(SetmealPageQueryDTO setmealPageQueryDTO);

    void edit(SetmealDTO setmealDTO);
}
