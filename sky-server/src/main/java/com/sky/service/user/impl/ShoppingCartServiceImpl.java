package com.sky.service.user.impl;

import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.user.DishMapper;
import com.sky.mapper.user.SetmealMapper;
import com.sky.mapper.user.ShoppingCartMapper;
import com.sky.service.user.ShoppingCartService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ShoppingCartServiceImpl implements ShoppingCartService {
    @Autowired
    ShoppingCartMapper shoppingCartMapper;
    @Autowired
    DishMapper dishMapper;
    @Autowired
    SetmealMapper setmealMapper;

    /**
     * 将某个菜品或套餐添加到购物车中 (或将其数量加1)
     * @param shoppingCartDTO 前端传递过来的参数，dishId / setmealId 其中一者不为null，dishFlavor 可能为null
     */
    @Override
    public void add(ShoppingCartDTO shoppingCartDTO) {
        ShoppingCart shoppingCart = new ShoppingCart();
        //前段传递过来的参数中没有当前用户的id，所以需要手动设置上 userId
        shoppingCart.setUserId(BaseContext.getCurrentId());
        //将 shoppingCartDTO 中的 dishId、setmealId、dishFlavor 拷贝到 shoppingCart 中，用于通用查询
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        //先判断当前菜品或套餐是否已经存在于购物车中
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);
        if(list != null && list.size() > 0) {
            //如果通用的查询得到的列表不为空，说明此商品已经存在于购物车，那么将其数量加1即可
            //此时的列表有且只有1个元素，这个元素就是当前的商品对象，注意与上面用于查询的 shoppingCart 区分开
            ShoppingCart curShoppingCart = list.get(0);
            curShoppingCart.setNumber(curShoppingCart.getNumber() + 1);
            shoppingCartMapper.updateById(curShoppingCart);
        }else {
            //如果不存在于购物车中，那么执行新增操作
            addShoppingCart(shoppingCart);
        }
    }

    /**
     * 将当前菜品或套餐添加到购物车中 (向数据库插入数据)
     * @param shoppingCart
     */
    public void addShoppingCart(ShoppingCart shoppingCart) {
        //由于前端传递过来的参数只有 dishId 或 setmealId，以及可能存在的 dishFlavor，所以其余参数需要去对应的表中进行查询
        Long dishId = shoppingCart.getDishId();
        Long setmealId = shoppingCart.getSetmealId();
        //判断当前添加的是菜品还是套餐，然后去对应的表中查询对应的数据，赋值给 ShoppingCart
        if(dishId != null) {
            Dish dish = dishMapper.selectById(dishId);
            shoppingCart.setName(dish.getName());
            shoppingCart.setAmount(dish.getPrice());
            shoppingCart.setDishId(dish.getId());
            shoppingCart.setImage(dish.getImage());
        }else {
            Setmeal setmeal = setmealMapper.selectById(setmealId);
            shoppingCart.setName(setmeal.getName());
            shoppingCart.setAmount(setmeal.getPrice());
            shoppingCart.setSetmealId(setmeal.getId());
            shoppingCart.setImage(setmeal.getImage());
        }
        //第一次添加，数量为1
        shoppingCart.setNumber(1);
        //这个实体类只有 updateTime，所以不能使用 mp 的自动填充或者 AOP 来填充公共字段，只能手动设置
        shoppingCart.setCreateTime(LocalDateTime.now());
        shoppingCartMapper.insert(shoppingCart);
    }

    @Override
    public List<ShoppingCart> checkShoppingCart() {
        //根据当前的用户id，查询当前用户的购物车，构建一个 ShoppingCart 并为其 userId 赋值，然后使用 “通用查询” 即可
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUserId(BaseContext.getCurrentId());
        return shoppingCartMapper.list(shoppingCart);
    }

    /**
     * 删除购物车中的某个商品 (有可能是令其数量减1)
     * @param shoppingCartDTO 前端传递过来的参数，dishId / setmealId 其中一者不为null，dishFlavor 可能为nul
     */
    @Override
    public void sub(ShoppingCartDTO shoppingCartDTO) {
        //先构造通用查询所使用的对象，进行通用查询，查询出当前的商品
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUserId(BaseContext.getCurrentId());
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);    
        //当前商品一定是存在的，判断其在购物车中的数量是否为1
        ShoppingCart curShoppingCart = shoppingCartMapper.list(shoppingCart).get(0);
        Integer number = curShoppingCart.getNumber();
        //如果当前商品的数量大于1，那么让其数量减少1即可
        if(number > 1) {
            curShoppingCart.setNumber(number - 1);
            shoppingCartMapper.updateById(curShoppingCart);
        }else {
            //如果当前商品数量等于1，那么将这个商品从购物车中删除
            shoppingCartMapper.deleteById(curShoppingCart);
        }
    }

    @Override
    public void cleanShoppingCart() {
        //清空 “当前用户” 的购物车
        shoppingCartMapper.deleteAll(BaseContext.getCurrentId());
    }
}
