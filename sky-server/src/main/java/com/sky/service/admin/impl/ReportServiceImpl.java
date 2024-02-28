package com.sky.service.admin.impl;

import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.entity.User;
import com.sky.mapper.admin.OrderDetailMapper;
import com.sky.mapper.admin.OrderMapper;
import com.sky.mapper.user.UserMapper;
import com.sky.service.admin.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReportServiceImpl implements ReportService {

    OrderMapper orderMapper;
    UserMapper userMapper;
    OrderDetailMapper orderDetailMapper;

    @Autowired
    public ReportServiceImpl(OrderMapper orderMapper, UserMapper userMapper, OrderDetailMapper orderDetailMapper) {
        this.orderMapper = orderMapper;
        this.userMapper = userMapper;
        this.orderDetailMapper = orderDetailMapper;
    }

    @Override
    public TurnoverReportVO turnoverStatistics(LocalDate begin, LocalDate end) {
        //查询 begin 到 end 这几天内的所有 “已完成” 订单
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN); // begin 的最早时间
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX); // end 的最晚时间
        List<Orders> orderList = orderMapper.getOrderTimeAndAmountBetween(beginTime, endTime);
        //如果此时间段没有任何订单，那么返回两个空的字符串
        if (orderList.size() == 0) {
            return new TurnoverReportVO("", "");
        }
        //先拼接 dateList
        String dateList = getDateListAsString(begin, end);
        //再拼接 turnoverList
        StringBuilder turnoverList = new StringBuilder();
        //起始日期为 beginTime 对应的日期
        LocalDate date = begin;
        int orderIndex = 0;
        Orders order = orderList.get(0);
        LocalDate orderDate = order.getOrderTime().toLocalDate();
        while (orderIndex < orderList.size()) {
            //某日的营业额
            BigDecimal total = new BigDecimal(0);
            //用一个循环处理掉日期早于当前订单日期的 date：如果 date 的日期早于当前订单，那么让 date 的日期向后推
            while (date.isBefore(orderDate)) {
                //当前 date 的营业额为0
                turnoverList.append("0,");
                date = date.plusDays(1);
            }
            //用一个循环累积相同日期的营业额：如果 date 与订单日期相同，将当前订单的营业额累加至 total 中
            while (date.isEqual(orderDate)) {
                total = total.add(order.getAmount());
                //累积完成后，订单向后迭代一位，迭代之前需判断是否越界
                if (++orderIndex >= orderList.size()) {
                    break;
                }
                order = orderList.get(orderIndex);
                orderDate = order.getOrderTime().toLocalDate();
            }
            //退出循环后，将当前累积出的营业额拼接上去，同时由于当前 date 的营业额已经记录过，将 date 后推一天
            turnoverList.append(total.toPlainString()).append(",");
            date = date.plusDays(1);
        }
        //订单列表遍历完成后，可能存在最后一个订单的日期小于最后日期的情况，此时需要将剩余日期对应的营业额(0)补齐
        while (date.isBefore(end) || date.isEqual(end)) {
            turnoverList.append("0,");
            date = date.plusDays(1);
        }
        return TurnoverReportVO
                .builder()
                .dateList(dateList)
                .turnoverList(turnoverList.deleteCharAt(turnoverList.length() - 1).toString())
                .build();
    }

    @Override
    public UserReportVO userStatistics(LocalDate begin, LocalDate end) {
        String dateList = getDateListAsString(begin, end);
        StringBuilder newUserList = new StringBuilder();
        StringBuilder totalUserList = new StringBuilder();
        //查出对应时间段注册的用户
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);
        List<User> userList = userMapper.getUserBetween(beginTime, endTime);
        //查出早于 beginTime 就已经存在的用户，作为总用户数的初始值
        Integer totalCount = userMapper.getTotalUserCountBefore(beginTime);
        //当前时间段不存在任何新用户时，直接返回
        if (userList.size() == 0) {
            for (LocalDate date = begin; date.isBefore(end) || date.isEqual(end); date = date.plusDays(1)) {
                newUserList.append("0").append(",");
                totalUserList.append(totalCount).append(",");
            }
            return UserReportVO.builder()
                    .dateList(dateList)
                    .newUserList(newUserList.deleteCharAt(newUserList.length() - 1).toString())
                    .totalUserList(totalUserList.deleteCharAt(totalUserList.length() - 1).toString())
                    .build();
        }
        int newCount = 0;
        int userIndex = 0;
        User user = userList.get(0);
        LocalDate date = begin;
        //遍历 userList
        while (userIndex < userList.size()) {
            LocalDate userCreateDate = user.getCreateTime().toLocalDate();
            //如果 date 早于当前用户的日期，那么让 date 持续向后推一天，直至二者相等
            while (date.isBefore(userCreateDate)) {
                //当前 date 的新增用户数为0，总用户数不变
                newUserList.append("0,");
                totalUserList.append(totalCount).append(",");
                date = date.plusDays(1);
            }
            //当 date 与当前用户的注册日期相同时，迭代用户，直至用户的注册日期超过 date
            while (date.isEqual(userCreateDate)) {
                //每遍历一位用户，新增用户数和总用户数都增加1
                newCount++;
                totalCount++;
                //迭代用户，迭代之前检查是否越界
                if (++userIndex >= userList.size()) {
                    break;
                }
                user = userList.get(userIndex);
                userCreateDate = user.getCreateTime().toLocalDate();
            }
            //退出循环，此时用户的日期晚于当前 date，记录当前 date 的新增用户数和总用户数
            newUserList.append(newCount).append(",");
            totalUserList.append(totalCount).append(",");
            newCount = 0; //重置新增用户数
            //当前 date 已经记录完毕，向后推一天
            date = date.plusDays(1);
        }
        //用户列表遍历完毕之后，处理剩余的日期
        while (date.isBefore(end) || date.isEqual(end)) {
            newUserList.append(0).append(",");
            totalUserList.append(totalCount).append(",");
            date = date.plusDays(1);
        }
        return UserReportVO.builder()
                .dateList(dateList)
                .newUserList(newUserList.deleteCharAt(newUserList.length() - 1).toString())
                .totalUserList(totalUserList.deleteCharAt(totalUserList.length() - 1).toString())
                .build();
    }

    @Override
    public SalesTop10ReportVO top10Statistics(LocalDate begin, LocalDate end) {
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);
        //按照升序查出当前时间段的销量前10菜品数据
        List<OrderDetail> top10ListAsc = orderDetailMapper.getSalesTop10DescBetween(beginTime, endTime);
        StringBuilder nameList = new StringBuilder();
        StringBuilder numberList = new StringBuilder();
        for (OrderDetail od : top10ListAsc) {
            nameList.append(od.getName()).append(",");
            numberList.append(od.getNumber()).append(",");
        }
        //如果查出的菜品数量不足10个，需要对 nameList 和 numberList 的剩余位置进行填充
        for (int i = top10ListAsc.size() + 1; i <= 10; i++) {
            nameList.append(",");
            numberList.append(0).append(",");
        }
        return SalesTop10ReportVO.builder()
                .nameList(nameList.deleteCharAt(nameList.length() - 1).toString())
                .numberList(numberList.deleteCharAt(numberList.length() - 1).toString())
                .build();
    }

    @Override
    public OrderReportVO ordersStatistics(LocalDate begin, LocalDate end) {
        String dateList = getDateListAsString(begin, end);
        StringBuilder orderCountList = new StringBuilder();
        StringBuilder validOrderCountList = new StringBuilder();
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);
        //初始化 beginTime 到 endTime 之间的总订单数和有效订单数
        Integer orderCount = 0;
        Integer validOrderCount = 0;
        double orderCompletionRate = 0.0;
        //查出 beginTime 到 endTime 之间的所有订单
        List<Orders> orderList = orderMapper.getOrdersBetween(beginTime, endTime);
        //如果 beginTime 到 endTime 内不存在任何订单，直接返回
        if (orderList.size() == 0) {
            for (LocalDate date = begin; date.isBefore(end) || date.isEqual(end); date = date.plusDays(1)) {
                orderCountList.append(0).append(",");
                validOrderCountList.append(0).append(",");
            }
            orderCountList.deleteCharAt(orderCountList.length() - 1);
            validOrderCountList.deleteCharAt(validOrderCountList.length() - 1);
            return new OrderReportVO(dateList, orderCountList.toString(), validOrderCountList.toString(), orderCount, validOrderCount, orderCompletionRate);
        }
        // beginTime 到 endTime 内存在订单
        LocalDate date = begin;
        int orderIndex = 0;
        Orders order = orderList.get(orderIndex);
        LocalDate orderDate = order.getOrderTime().toLocalDate();
        while (orderIndex < orderList.size()) {
            while (date.isBefore(orderDate)) {
                orderCountList.append(orderCount).append(",");
                validOrderCountList.append(validOrderCount).append(",");
                date = date.plusDays(1);
            }
            while (date.isEqual(orderDate)) {
                orderCount++;
                if (order.getStatus().equals(Orders.COMPLETED)) {
                    validOrderCount++;
                }
                if (++orderIndex >= orderList.size()) {
                    break;
                }
                order = orderList.get(orderIndex);
                orderDate = order.getOrderTime().toLocalDate();
            }
            orderCountList.append(orderCount).append(",");
            validOrderCountList.append(validOrderCount).append(",");
            date = date.plusDays(1);
        }
        orderCompletionRate = validOrderCount.doubleValue() / orderCount.doubleValue();
        //处理剩余的日期
        while (date.isBefore(end) || date.isEqual(end)) {
            orderCountList.append(orderCount).append(",");
            validOrderCountList.append(validOrderCount).append(",");
            date = date.plusDays(1);
        }
        return OrderReportVO.builder()
                .dateList(dateList)
                .orderCountList(orderCountList.deleteCharAt(orderCountList.length() - 1).toString())
                .validOrderCountList(validOrderCountList.deleteCharAt(validOrderCountList.length() - 1).toString())
                .totalOrderCount(orderCount)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .build();
    }

    @Override
    public void exportExcel(HttpServletResponse response) {
        //起始日期为：当前日期 - 30天
        LocalDate begin = LocalDate.now().minusDays(30);
        //结束日期为：当前日期的前一天，当前日期不计入统计
        LocalDate end = LocalDate.now().minusDays(1);
        //日期列表
        String[] dateList = getDateListAsString(begin, end).split(",");
        //营业额列表
        String[] turnoverList = turnoverStatistics(begin, end).getTurnoverList().split(",");
        OrderReportVO orderReportVO = ordersStatistics(begin, end);
        //有效订单列表
        String[] validOrderCountList = orderReportVO.getValidOrderCountList().split(",");
        //总订单数列表
        String[] orderCountList = orderReportVO.getOrderCountList().split(",");
        //订单完成率列表
        String[] orderCompletionList = new String[orderCountList.length];
        //平均客单价列表
        String[] averageAmountList = new String[orderCountList.length];
        for(int i = 0; i < orderCompletionList.length; i++) {
            orderCompletionList[i] = Integer.parseInt(orderCountList[i]) == 0 ? "0.0" :
                    String.valueOf(Double.parseDouble(validOrderCountList[i]) / Double.parseDouble(orderCountList[i]));
            averageAmountList[i] = Integer.parseInt(validOrderCountList[i]) == 0 ? "0.0" :
                    String.valueOf(Double.parseDouble(turnoverList[i]) / Double.parseDouble(validOrderCountList[i]));
        }
        //新增用户数列表
        String[] newUserList = userStatistics(begin, end).getNewUserList().split(",");
        //总营业额
        Double total = Arrays.stream(turnoverList).map(Double::parseDouble).reduce(0.0, Double::sum);
        //总有效订单数
        Integer totalValidOrderCount = Arrays.stream(validOrderCountList).map(Integer::parseInt).reduce(0, Integer::sum);
        //总订单完成率
        Integer totalOrderCount = Arrays.stream(orderCountList).map(Integer::parseInt).reduce(0, Integer::sum);
        Double totalCompletionRate = totalValidOrderCount.doubleValue() / totalOrderCount.doubleValue();
        //总新增用户数
        Integer totalNewUserCount = Arrays.stream(newUserList).map(Integer::parseInt).reduce(0, Integer::sum);
        //总平均客单价
        Double totalAvrAmount = total / totalValidOrderCount;

        //填充数据
        InputStream inputStream = null;
        XSSFWorkbook excel = null;
        ServletOutputStream outputStream = null;
        try {
            //1、从 resources 目录下读取模板文件
            inputStream = this.getClass().getClassLoader().getResourceAsStream("template/运营数据报表模板.xlsx");
            //拿到 excel 文件对象
            excel = new XSSFWorkbook(inputStream);
            //拿到 sheet 对象
            XSSFSheet sheet = excel.getSheetAt(0);
            //填充当前报表的时间
            sheet.getRow(1).getCell(1).setCellValue("时间：" + begin + "至" + end);
            //填充概览数据
            XSSFRow row3 = sheet.getRow(3);
            row3.getCell(2).setCellValue(total);
            row3.getCell(4).setCellValue(totalCompletionRate);
            row3.getCell(6).setCellValue(totalNewUserCount);
            XSSFRow row4 = sheet.getRow(4);
            row4.getCell(2).setCellValue(totalValidOrderCount);
            row4.getCell(4).setCellValue(totalAvrAmount);
            //填充明细数据
            for(int i = 0; i < dateList.length; i++) {
                XSSFRow row = sheet.getRow(7 + i);
                row.getCell(1).setCellValue(dateList[i]);
                row.getCell(2).setCellValue(turnoverList[i]);
                row.getCell(3).setCellValue(validOrderCountList[i]);
                row.getCell(4).setCellValue(orderCompletionList[i]);
                row.getCell(5).setCellValue(averageAmountList[i]);
                row.getCell(6).setCellValue(newUserList[i]);
            }
            //通过输出流将 Excel 文件下载到客户端浏览器
            outputStream = response.getOutputStream();
            excel.write(outputStream);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            try {
                inputStream.close();
                excel.close();
                outputStream.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }


    /**
     * 根据传入的 begin 和 end，以逗号为间隔，将每个日期拼接在一起
     *
     * @param begin 起始日期
     * @param end   结束日期
     * @return 拼接后的字符串
     */
    private String getDateListAsString(LocalDate begin, LocalDate end) {
        StringBuilder dateList = new StringBuilder();
        for (LocalDate ld = begin; ld.isBefore(end) || ld.isEqual(end); ld = ld.plusDays(1)) {
            dateList.append(ld.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))).append(",");
        }
        return dateList.deleteCharAt(dateList.length() - 1).toString();
    }
}
