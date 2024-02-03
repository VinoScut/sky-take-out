package com.sky.service.admin.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.mapper.admin.CategoryMapper;
import com.sky.mapper.admin.DishFlavorMapper;
import com.sky.mapper.admin.DishMapper;
import com.sky.mapper.admin.SetMealDishMapper;
import com.sky.result.PageResult;
import com.sky.service.admin.DishService;
import com.sky.vo.DishVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DishServiceImpl implements DishService {
    @Autowired
    DishMapper dishMapper;

    @Autowired
    CategoryMapper categoryMapper;

    @Autowired
    DishFlavorMapper dishFlavorMapper;

    @Autowired
    SetMealDishMapper setMealDishMapper;

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

    @Override
    @Transactional
    public void add(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        //先完成 dish 的新增操作，获取到当前这个 dish 的自增主键，这样在下面才能为 dishFlavor 的 dishId 进行赋值
        dishMapper.insert(dish);
        List<DishFlavor> flavors = dishDTO.getFlavors();
        flavors.forEach(dishFlavor -> {
            dishFlavor.setDishId(dish.getId());
            dishFlavorMapper.insert(dishFlavor);
        });
    }

    @Override
    @Transactional
    public int delete(String ids) {
        //(1)检查是否存在 “启售状态” 的菜品，正在销售的菜品不能删除
        //(2)检查当前菜品是否关联在某个 “套餐” 中
        //(3)排除掉以上两种情况的菜品后，剩余的菜品可以删除，但要先删除引用了当前菜品的主键的 dishFlavor从表 中的数据，然后再删除菜品
        List<Long> dishToDelete = Arrays.stream(ids.split(","))
                                        .map(Long::parseLong)
                                        .filter(id -> dishMapper.checkStatus(id) == 0)
                                        .filter(id -> setMealDishMapper.selectByDishId(id) == null)
                                        .collect(Collectors.toList());
        //仅当要删除的菜品数量大于1时，才进行删除操作
        //之所以进行一次判断，是因为在 mp 中，wrapper 和 deleteBatchIds 都要求列表不为空，否则会出错
        if(dishToDelete.size() > 0) {
            //先删除 从表dishFlavor 中的数据
//            LambdaQueryWrapper<DishFlavor> dishFlavorLambdaQueryWrapper = new LambdaQueryWrapper<>();
//            dishFlavorLambdaQueryWrapper.in(DishFlavor::getDishId, dishToDelete);
//            dishFlavorMapper.delete(dishFlavorLambdaQueryWrapper);
            dishFlavorMapper.deleteByDishIds(dishToDelete);
            //再删除 主表dish 中的数据
            dishMapper.deleteBatchIds(dishToDelete);
        }
        return dishToDelete.size();
    }

    @Override
    public void enableOrDisable(Long id, Integer status) {
        Dish dish = new Dish();
        dish.setId(id);
        dish.setStatus(status);
        dishMapper.updateById(dish);
    }

    @Override
    @Transactional
    public void edit(DishDTO dishDTO) {
        //将 dishDTO 中除了 flavors 之外的属性全部拷贝到 dish 对象中，然后执行修改操作
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        dishMapper.updateById(dish);
        Long dishId = dishDTO.getId();
        //拿到当前菜品的新口味数据，并为每个新口味的 dishId 属性赋值，为可能的添加操作做准备，因为前端传过来的口味数据是没有 dishId 的
        List<DishFlavor> newFlavors = dishDTO.getFlavors();
        newFlavors.forEach(newFlavor -> newFlavor.setDishId(dishId));
        //从数据库中查出旧的口味数据，并为每个旧口味的 dishId 进行赋值，后面删除旧口味数据时会用到
        Map<String, DishFlavor> oldFlavors = dishFlavorMapper.getDishFlavorByDishId(dishId);
        oldFlavors.values().forEach(oldFlavor -> oldFlavor.setDishId(dishId));
        //遍历新口味数据
        for(DishFlavor newFlavor : newFlavors) {
            String newFlavorName = newFlavor.getName();
            String newFlavorValue = newFlavor.getValue();
            //如果在旧口味中 “没有” 这个新口味，那么将这个新口味添加到 dish_flavor 这张表中
            if(!oldFlavors.containsKey(newFlavorName)) {
                dishFlavorMapper.insert(newFlavor);
                continue;
            }
            //如果当前这个口味已经存在，判断是否发生了改变，如果发生了，那么执行修改操作
            if(!newFlavorValue.equals(oldFlavors.get(newFlavorName).getValue())) {
                dishFlavorMapper.updateByDishIdAndName(newFlavor);
            }
            //无论是否发生改变，都需要在旧口味中将当前口味删除掉
            oldFlavors.remove(newFlavorName);
        }
        //此时旧口味数据中剩下的口味，在新口味中不存在，说明这些口味是需要被删除的
        oldFlavors.values().forEach(dishFlavorMapper::deleteByDishIdAndName);


//        //从数据库中将当前菜品的旧口味数据查出来
//        Map<String, String> oldFlavors = dishFlavorMapper.getNameAndValueByDishId(dishId);
//        //遍历新口味数据
//        for(DishFlavor newFlavor : dishDTO.getFlavors()) {
//            String name = newFlavor.getName();
//            String value = newFlavor.getValue();
//            if(!oldFlavors.containsKey(name)) {
//                //如果旧口味中不存在当前口味，执行“新增”操作
//                dishFlavorMapper.insert(newFlavor);
//                continue;
//            }
//            //如果当前遍历到的口味也在旧口味中存在并且有修改过，那么执行“修改”操作，然后将旧口味中的对应的口味数据剔除掉
//            if(!value.equals(oldFlavors.get(name))) {
//                dishFlavorMapper.update(newFlavor);
//                oldFlavors.remove(name);
//            }
//        }
//        //如果旧口味的 map 中仍然有数据，说明这些口味在当前菜品的修改操作中被删除，将这些口味从 dish_flavor 表中删除
//        for(String name: oldFlavors.keySet()) {
//            DishFlavor dishFlavorToDelete = new DishFlavor();
//            dishFlavorToDelete.setDishId(dishId);
//            dishFlavorToDelete.setName(name);
//            dishFlavorMapper.deleteByDishIdAndName(dishFlavorToDelete);
//        }
    }

    @Override
    public List<Dish> selectDishByCategoryId(Long categoryId) {
        List<Dish> dishes = dishMapper.selectByCategoryId(categoryId);
        return dishes;
    }
}
