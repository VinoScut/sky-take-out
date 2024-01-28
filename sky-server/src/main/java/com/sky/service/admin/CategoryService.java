package com.sky.service.admin;

import com.sky.dto.CategoryDTO;
import com.sky.entity.Category;
import com.sky.result.PageResult;

import java.util.List;

public interface CategoryService {
    PageResult<Category> page(String name, Integer page, Integer pageSize, Integer type);

    void update(CategoryDTO categoryDTO);

    void enableOrDisable(Long id, Integer status);

    void insert(CategoryDTO categoryDTO);

    void delete(Long id);

    List<Category> selectByType(Integer type);
}
