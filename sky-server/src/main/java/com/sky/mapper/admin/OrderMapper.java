package com.sky.mapper.admin;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import com.sky.vo.OrderVO;
import org.apache.ibatis.annotations.Select;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("adminOrderMapper")
public interface OrderMapper extends BaseMapper<Orders> {

    Page<OrderVO> page(Page<OrderVO> orderVOPage, OrdersPageQueryDTO ordersPageQueryDTO);

    @Select("select count(*) from orders where status = #{status}")
    Integer getCountByStatus(Integer status);

    @Select("select * from orders where id = #{orderId}")
    Orders selectById(Long orderId);

    @Select("select * from orders where status = #{status}")
    @Cacheable(cacheNames = "orderList", key = "'01'")
    List<Orders> getOrdersByStatus(Integer status);
}