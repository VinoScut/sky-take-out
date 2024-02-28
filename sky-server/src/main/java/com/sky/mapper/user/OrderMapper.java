package com.sky.mapper.user;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import com.sky.vo.OrderVO;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderMapper extends BaseMapper<Orders> {

    /**
     * 将对应订单的状态改为 TO_BE_CONFIRMED，支付状态改为 PAID
     *
     * @param orderNumber 订单号
     */
    void paymentSuccess(String orderNumber, Orders orders);

    Page<OrderVO> page(Page<OrderVO> orderVOPage, OrdersPageQueryDTO ordersPageQueryDTO, Long userId);

    OrderVO selectByOrderId(Long orderId);

    @Select("select * from orders where number = #{orderNumber}")
    Orders selectByOrderNumber(String orderNumber);
}
