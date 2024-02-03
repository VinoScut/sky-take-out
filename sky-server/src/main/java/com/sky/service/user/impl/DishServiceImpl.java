package com.sky.service.user.impl;

import com.sky.mapper.user.CategoryMapper;
import com.sky.mapper.user.DishMapper;
import com.sky.service.user.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service("userDishServiceImpl")
@Slf4j
public class DishServiceImpl implements DishService {
    @Autowired
    DishMapper dishMapper;
    @Autowired
    CategoryMapper categoryMapper;
    @Resource(name = "redisTemplate")
    ListOperations<String, DishVO> listOperations;

    /**
     * 根据分类id，查询其中的菜品信息
     * @param categoryId 分类id
     * @return 当前分类中的所有菜品
     */
    @Override
    public List<DishVO> getDishByCategoryId(Long categoryId) {
        //先去 redis 中查询，如果查询到，直接返回
        //当前分类在 redis 中的 key，规则为： category_"分类id"
        String key = "category_" + categoryId;
        List<DishVO> dishVOList = listOperations.range(key, 0, -1);
        //如果 redis 中没查到，那么去数据库中查询，并放入 redis 中进行缓存
        if(dishVOList == null || dishVOList.size() == 0) {
            dishVOList = getDishByCategoryIdFromDataBase(categoryId);
            listOperations.leftPushAll(key, dishVOList);
        }
        return dishVOList;
    }

    /**
     * 如果 redis 中未命中当前分类对应的缓存，那么去数据库中查询当前分类的菜品信息
     * @param categoryId 分类 id
     * @return 当前分类中的所有菜品
     */
    public List<DishVO> getDishByCategoryIdFromDataBase(Long categoryId) {
        List<DishVO> dishVOList = dishMapper.getDishByCategoryId(categoryId);
        if(dishVOList != null && dishVOList.size() > 0) {
            String categoryName = categoryMapper.getCategoryName(dishVOList.get(0).getCategoryId());
            dishVOList.forEach(dishVO -> dishVO.setCategoryName(categoryName));
        }
        return dishVOList;
    }
}
