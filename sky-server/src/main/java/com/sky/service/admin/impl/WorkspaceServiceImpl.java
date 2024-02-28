package com.sky.service.admin.impl;

import com.sky.entity.Dish;
import com.sky.entity.Orders;
import com.sky.entity.Setmeal;
import com.sky.mapper.admin.DishMapper;
import com.sky.mapper.admin.OrderMapper;
import com.sky.mapper.admin.SetMealMapper;
import com.sky.mapper.user.UserMapper;
import com.sky.service.admin.WorkspaceService;
import com.sky.vo.BusinessDataVO;
import com.sky.vo.DishOverViewVO;
import com.sky.vo.OrderOverViewVO;
import com.sky.vo.SetmealOverViewVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class WorkspaceServiceImpl implements WorkspaceService {

    UserMapper userMapper;
    OrderMapper orderMapper;
    SetMealMapper setMealMapper;
    DishMapper dishMapper;

    @Autowired
    public WorkspaceServiceImpl(UserMapper userMapper, OrderMapper orderMapper, SetMealMapper setMealMapper, DishMapper dishMapper) {
        this.userMapper = userMapper;
        this.orderMapper = orderMapper;
        this.setMealMapper = setMealMapper;
        this.dishMapper = dishMapper;
    }

    @Override
    public BusinessDataVO todayBusinessData() {
        // beginTime 指定为当前日期的伊始
        LocalDateTime beginTime = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        // endTime 指定为当前时间
        LocalDateTime endTime = LocalDateTime.now();
        int newUsers = userMapper.getUserBetween(beginTime, endTime).size();
        List<Orders> orderList = orderMapper.getOrdersBetween(beginTime, endTime);
        double turnover = 0.0; //营业额
        int validOrderCount = 0; //有效订单数
        double unitPrice;
        double orderCompletionRate;
        for (Orders order : orderList) {
            if(order.getStatus().equals(Orders.COMPLETED)) {
                validOrderCount++;
                turnover += order.getAmount().doubleValue();
            }
        }
        //平均客单价 = 营业额 / 顾客数(有效订单数)
        unitPrice = turnover / validOrderCount;
        orderCompletionRate = validOrderCount / (double) orderList.size();
        return BusinessDataVO.builder()
                .newUsers(newUsers)
                .turnover(turnover)
                .unitPrice(unitPrice)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .build();
    }

    @Override
    public SetmealOverViewVO setmealOverview() {
        List<Setmeal> setmealList = setMealMapper.getSetmealList();
        int sold = 0;
        for (Setmeal setmeal : setmealList) {
            if(setmeal.getStatus() == 1) {
                sold++;
            }
        }
        return SetmealOverViewVO.builder()
                .sold(sold)
                .discontinued(setmealList.size() - sold)
                .build();
    }

    @Override
    public DishOverViewVO dishOverview() {
        List<Dish> dishList = dishMapper.getDishList();
        int sold = 0;
        for (Dish dish : dishList) {
            if(dish.getStatus() == 1) {
                sold++;
            }
        }
        return DishOverViewVO.builder()
                .sold(sold)
                .discontinued(dishList.size() - sold)
                .build();
    }

    @Override
    public OrderOverViewVO orderOverview() {
        LocalDateTime beginTime = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.now();
        List<Orders> orderList = orderMapper.getOrdersBetween(beginTime, endTime);
        int cancelledOrders = 0, completedOrders = 0, deliveredOrders = 0, waitingOrders = 0;
        for (Orders order : orderList) {
            switch (order.getStatus()) {
                case 6:
                    cancelledOrders++;
                    break;
                case 5:
                    completedOrders++;
                    break;
                case 3:
                    deliveredOrders++;
                    break;
                case 2:
                    waitingOrders++;
                    break;
            }
        }
        return OrderOverViewVO.builder()
                .allOrders(orderList.size())
                .cancelledOrders(cancelledOrders)
                .completedOrders(completedOrders)
                .deliveredOrders(deliveredOrders)
                .waitingOrders(waitingOrders)
                .build();
    }
}
