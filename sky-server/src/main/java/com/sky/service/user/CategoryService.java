package com.sky.service.user;

import com.sky.entity.Category;

import java.util.List;

public interface CategoryService {

    List<Category> getCategory(Integer type);
}
