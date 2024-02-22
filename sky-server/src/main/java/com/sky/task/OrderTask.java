package com.sky.task;

import com.sky.entity.Orders;
import com.sky.mapper.admin.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

@Component //定时任务类需要交给 Spring 容器管理
@Slf4j
public class OrderTask {

    @Resource(name = "redisTemplate")
    RedisTemplate<String, List<Orders>> redisTemplate;
    @Autowired
    OrderMapper orderMapper;


    /**
     * 每分钟检查一次超时未支付的订单，超过5分钟未支付的订单将被取消
     */
    @Scheduled(cron = "0 * * * * ?")
    public void processTimeoutUnpaidOrders() {
        log.info("定时任务启动：检查超时未支付订单...");
        List<Orders> unpaidOrderList = orderMapper.getOrdersByStatus(Orders.PENDING_PAYMENT);
        unpaidOrderList.forEach(uo -> {
            LocalDateTime orderTime = uo.getOrderTime();
            //如果用户5分钟内未支付订单，则取消该订单
            if(orderTime.isBefore(LocalDateTime.now().minusMinutes(5))) {
                //用户超时未支付，取消此订单
                uo.setStatus(Orders.CANCELLED);
                uo.setCancelTime(LocalDateTime.now());
                uo.setCancelReason("订单超时未支付，自动取消");
                orderMapper.updateById(uo);
                //清除 redis 中的缓存
                redisTemplate.delete("orderList::01");
                log.info("取消了超时未支付的订单，清除redis缓存");
            }
        });
    }

    /**
     * 每天凌晨1点检查是否存在 “派送中” 的订单，如果存在，将这些订单标记为 “已完成”
     *
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void processDeliveryInProgressOrder() {
        log.info("定时任务启动：检查仍在派送中订单...");
        List<Orders> inDeliveryOrderList = orderMapper.getOrdersByStatus(Orders.DELIVERY_IN_PROGRESS);
        inDeliveryOrderList.forEach(ido -> {
            ido.setStatus(Orders.COMPLETED);
            orderMapper.updateById(ido);
            //手动清除 redis 中订单相关的缓存
            redisTemplate.delete("orderList::01");
        });
    }

}
