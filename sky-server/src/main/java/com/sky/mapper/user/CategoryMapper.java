package com.sky.mapper.user;

import com.sky.entity.Category;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("userCategoryMapper")
public interface CategoryMapper {

    @Select("select name from category where id = #{categoryId}")
    String getCategoryName(Long categoryId);

    List<Category> getCategory(Integer type);
}
