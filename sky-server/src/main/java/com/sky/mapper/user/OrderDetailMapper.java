package com.sky.mapper.user;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sky.entity.OrderDetail;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderDetailMapper extends BaseMapper<OrderDetail> {

    void batchInsert(List<OrderDetail> orderDetailList);
}
