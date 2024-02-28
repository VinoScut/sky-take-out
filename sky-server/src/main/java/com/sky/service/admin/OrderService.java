package com.sky.service.admin;


import com.sky.dto.*;
import com.sky.result.PageResult;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderVO;
import com.sky.vo.TurnoverReportVO;

public interface OrderService {

    PageResult<OrderVO> page(OrdersPageQueryDTO ordersPageQueryDTO);

    OrderStatisticsVO statistic();

    void cancel(OrdersCancelDTO ordersCancelDTO);

    void rejection(OrdersRejectionDTO ordersRejectionDTO);

    void confirm(OrdersConfirmDTO ordersConfirmDTO);

    void complete(Long id);

    void delivery(Long id);

}
