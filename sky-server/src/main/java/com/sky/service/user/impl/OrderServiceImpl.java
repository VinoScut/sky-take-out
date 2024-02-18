package com.sky.service.user.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.AddressBook;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.entity.ShoppingCart;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.user.*;
import com.sky.result.PageResult;
import com.sky.service.user.OrderService;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    OrderMapper orderMapper;

    UserMapper userMapper;

    AddressBookMapper addressBookMapper;

    ShoppingCartMapper shoppingCartMapper;

    OrderDetailMapper orderDetailMapper;

    @Autowired
    public OrderServiceImpl(OrderMapper orderMapper, UserMapper userMapper, AddressBookMapper addressBookMapper, ShoppingCartMapper shoppingCartMapper, OrderDetailMapper orderDetailMapper) {
        this.orderMapper = orderMapper;
        this.userMapper = userMapper;
        this.addressBookMapper = addressBookMapper;
        this.shoppingCartMapper = shoppingCartMapper;
        this.orderDetailMapper = orderDetailMapper;
    }

    @Override
    @Transactional //涉及多表操作，需要加上 “事务相关” 的注解
    public OrderSubmitVO submit(OrdersSubmitDTO ordersSubmitDTO) {
        Long userId = BaseContext.getCurrentId();
        Long addressBookId = ordersSubmitDTO.getAddressBookId();
        //(1) 对可能存在的“业务异常”进行处理 (用户地址为空、购物车为空)
        AddressBook addressBook = addressBookMapper.selectById(addressBookId);
        if (addressBook == null) {
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }
        //构造一个 ShoppingCart 对象，用于查询 “当前用户” 的购物车 (故查询前只为其 userId 赋值)
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUserId(userId);
        List<ShoppingCart> shoppingCartList = shoppingCartMapper.list(shoppingCart);
        if (shoppingCartList == null || shoppingCartList.size() == 0) {
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }
        //(2) 在将订单对象插入数据库之前，我们需要对 Order 对象的部分属性进行填充，因为前端只传递过来了部分参数
        Orders orders = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO, orders);
        //订单号：使用 UUID
        String orderNumber = UUID.randomUUID().toString();
        orders.setNumber(orderNumber);
        //订单状态为：待付款
        orders.setStatus(Orders.PENDING_PAYMENT);
        orders.setUserId(userId);
        //下单时间
        LocalDateTime orderSubmitTime = LocalDateTime.now();
        orders.setOrderTime(orderSubmitTime);
        //支付状态为：未付款
        orders.setPayStatus(Orders.UN_PAID);
        //从user表中查出当前用户的 userName
        orders.setUserName(userMapper.getUserNameById(userId));
        //从上面得到的 addressBook 中获得 consignee、phone 和 address
        orders.setPhone(addressBook.getPhone());
        orders.setAddress(addressBook.getDetail());
        orders.setConsignee(addressBook.getConsignee());
        //填充完毕后，将 orders 插入数据库中
        orderMapper.insert(orders);
        //(3) 用户下单后，要更新订单详情，也就是将当前用户的购物车清空，对应的购物车项变为订单项
        List<OrderDetail> orderDetailList = new LinkedList<>();
        shoppingCartList.forEach(cart -> {
            OrderDetail orderDetail = new OrderDetail();
            //注意：主键对应的属性不拷贝
            BeanUtils.copyProperties(cart, orderDetail, "id");
            //设置订单详情项的订单id
            orderDetail.setOrderId(orders.getId());
            orderDetailList.add(orderDetail);
        });
        //批量插入 orderDetail
        orderDetailMapper.batchInsert(orderDetailList);
        //清空当前用户的购物车
        shoppingCartMapper.deleteAll(userId);
        //(4) 构造 OrderSubmitVO 并返回
        return OrderSubmitVO.builder()
                .orderAmount(orders.getAmount())
                .orderNumber(orderNumber)
                .orderTime(orderSubmitTime)
                .build();
    }

    @Override
    public void paymentSuccess(String orderNumber) {
        Orders orders = new Orders();
        //设置当前订单的结账时间、状态、支付状态
        orders.setCheckoutTime(LocalDateTime.now());
        orders.setStatus(Orders.TO_BE_CONFIRMED);
        orders.setPayStatus(Orders.PAID);
        orderMapper.paymentSuccess(orderNumber, orders);
    }

    @Override
    public void repetition(Long orderId) {
        //通过订单id查询到该订单对应的所有订单项，将这些订单项对应的菜品添加到当前用户的购物车中
        List<OrderDetail> orderDetailList = orderDetailMapper.listByOrderId(orderId);
        Long userId = BaseContext.getCurrentId();
        List<ShoppingCart> shoppingCartList = orderDetailList.stream()
                .map(od -> {
                    ShoppingCart shoppingCart = new ShoppingCart();
                    //注意：主键不需要拷贝
                    BeanUtils.copyProperties(od, shoppingCart, "id");
                    shoppingCart.setUserId(userId);
                    shoppingCart.setCreateTime(LocalDateTime.now());
                    return shoppingCart;
                })
                .collect(Collectors.toList());
        shoppingCartMapper.batchInsert(shoppingCartList);
    }

    @Override
    public PageResult<OrderVO> page(OrdersPageQueryDTO ordersPageQueryDTO) {
        Page<OrderVO> orderVOPage = new Page<>(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
        orderVOPage = orderMapper.page(orderVOPage, ordersPageQueryDTO, BaseContext.getCurrentId());
        return new PageResult<>(orderVOPage.getTotal(), orderVOPage.getRecords());
    }

    @Override
    public OrderVO orderDetail(Long orderId) {
        OrderVO orderVO = orderMapper.selectByOrderId(orderId);
        return orderVO;
    }

    /**
     * 客户取消订单
     *
     * @param id 订单id
     */
    @Override
    public void cancel(Long id) {
        Orders orders = orderMapper.selectById(id);
        if (orders == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        Integer status = orders.getStatus();
        //除了客户未付款，商家未确定这两种情况外，其他情况需要取消订单时，需要和商家电话联系，不能直接取消
        if (status > 2) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        //如果客户已付款，商家未确认订单，此时取消订单还需要为客户退款
        if (status == Orders.TO_BE_CONFIRMED) {
            //退款
        }
        //取消订单
        orders.setCancelTime(LocalDateTime.now());
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelReason("用户取消订单");
        orderMapper.updateById(orders);
    }
}
