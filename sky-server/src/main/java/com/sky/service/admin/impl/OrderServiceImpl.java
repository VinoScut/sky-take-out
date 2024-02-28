package com.sky.service.admin.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sky.constant.MessageConstant;
import com.sky.dto.*;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.exception.OrderBusinessException;
import com.sky.mapper.admin.OrderDetailMapper;
import com.sky.mapper.admin.OrderMapper;
import com.sky.result.PageResult;
import com.sky.service.admin.OrderService;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderVO;
import com.sky.vo.TurnoverReportVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@Service("adminOrderService")
public class OrderServiceImpl implements OrderService {

    OrderMapper orderMapper;

    OrderDetailMapper orderDetailMapper;

    @Autowired
    public OrderServiceImpl(OrderMapper orderMapper, OrderDetailMapper orderDetailMapper) {
        this.orderMapper = orderMapper;
        this.orderDetailMapper = orderDetailMapper;
    }

    @Override
    public PageResult<OrderVO> page(OrdersPageQueryDTO ordersPageQueryDTO) {
        Page<OrderVO> orderVOPage = new Page<>(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
        orderVOPage = orderMapper.page(orderVOPage, ordersPageQueryDTO);
        List<OrderVO> orderVOList = orderVOPage.getRecords();
        orderVOList.forEach(orderVO -> {
            String dishStr = getDishString(orderVO.getId());
            orderVO.setOrderDishes(dishStr);
        });
        return new PageResult<>(orderVOPage.getTotal(), orderVOList);
    }

    @Override
    public OrderStatisticsVO statistic() {
        OrderStatisticsVO orderStatisticsVO = new OrderStatisticsVO();
        orderStatisticsVO.setConfirmed(orderMapper.getCountByStatus(Orders.CONFIRMED));
        orderStatisticsVO.setToBeConfirmed(orderMapper.getCountByStatus(Orders.TO_BE_CONFIRMED));
        orderStatisticsVO.setDeliveryInProgress(orderMapper.getCountByStatus(Orders.DELIVERY_IN_PROGRESS));
        return orderStatisticsVO;
    }

    @Override
    public void cancel(OrdersCancelDTO ordersCancelDTO) {
        //先拿到当前的订单对象
        Orders orders = orderMapper.selectById(ordersCancelDTO.getId());
        orders.setStatus(Orders.CANCELLED);
        //设置此对象的 cancelReason 属性，然后交由 mp 进行 update
        orders.setCancelReason(ordersCancelDTO.getCancelReason());
        orderMapper.updateById(orders);
    }

    @Override
    public void rejection(OrdersRejectionDTO ordersRejectionDTO) {
        //先拿到当前的订单对象
        Orders orders = orderMapper.selectById(ordersRejectionDTO.getId());
        //商家只能拒绝待确认的订单
        if (!orders.getStatus().equals(Orders.TO_BE_CONFIRMED)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        //为用户退款

        orders.setStatus(Orders.CANCELLED);
        //设置此对象的 cancelReason 属性，然后交由 mp 进行 update
        orders.setRejectionReason(ordersRejectionDTO.getRejectionReason());
        orders.setCancelTime(LocalDateTime.now());
        ;
        orderMapper.updateById(orders);
    }

    @Override
    public void confirm(OrdersConfirmDTO ordersConfirmDTO) {
        Long orderId = ordersConfirmDTO.getId();
        Orders orders = orderMapper.selectById(orderId);
        //校验订单状态：只有待确认的订单商家才能进行确认
        if (orders == null || !orders.getStatus().equals(Orders.TO_BE_CONFIRMED)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        orders.setStatus(Orders.CONFIRMED);
        orderMapper.updateById(orders);
    }

    @Override
    public void complete(Long id) {
        Orders orders = orderMapper.selectById(id);
        //校验订单状态：只有派送中的订单，商家执行完成操作
        if (orders == null || !orders.getStatus().equals(Orders.DELIVERY_IN_PROGRESS)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        orders.setStatus(Orders.COMPLETED);
        orderMapper.updateById(orders);
    }

    @Override
    public void delivery(Long id) {
        Orders orders = orderMapper.selectById(id);
        //校验订单状态：只有待派送的订单商家才能进行派送
        if (orders == null || !orders.getStatus().equals(Orders.CONFIRMED)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        orders.setStatus(Orders.DELIVERY_IN_PROGRESS);
        orderMapper.updateById(orders);
    }


    public String getDishString(Long orderId) {
        //根据当前订单id查出所有的订单项
        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(orderId);
        //利用 stream API，将订单项映射为对应的字符串
        List<String> stringList = orderDetailList.stream()
                .map(orderDetail -> orderDetail.getName() + "*" + orderDetail.getNumber() + ";")
                .collect(Collectors.toList());
        //将所有订单项对应的字符串拼接为菜品字符串
        return String.join(" ", stringList);
    }
}
