package com.sky.mapper.user;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShoppingCartMapper extends BaseMapper<ShoppingCart> {

    @Delete("delete from shopping_cart where user_id = #{userId}")
    void deleteAll(Long userId);

    /**
     * 通用的查询购物车方法 —— 以传入的对象属性为条件，进行通用查询
     * @param shoppingCart 由查询条件封装起来的 ShoppingCart 对象
     * @return 查询结果集
     */
    List<ShoppingCart> list(ShoppingCart shoppingCart);
}
