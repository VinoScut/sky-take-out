package com.sky.service.user;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.result.PageResult;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;

public interface OrderService {

    OrderSubmitVO submit(OrdersSubmitDTO ordersSubmitDTO);

    void paymentSuccess(String orderNumber);

    void repetition(Long orderId);

    PageResult<OrderVO> page(OrdersPageQueryDTO ordersPageQueryDTO);

    OrderVO orderDetail(Long orderId);

    void cancel(Long id);

    void reminder(Long id);
}
