package com.sky.mapper.admin;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sky.entity.OrderDetail;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository("adminOrderDetailMapper")
public interface OrderDetailMapper extends BaseMapper<OrderDetail> {

    @Select("select * from order_detail where order_id = #{orderId}")
    List<OrderDetail> getByOrderId(Long orderId);

    List<OrderDetail> getSalesTop10DescBetween(LocalDateTime beginTime, LocalDateTime endTime);
}
