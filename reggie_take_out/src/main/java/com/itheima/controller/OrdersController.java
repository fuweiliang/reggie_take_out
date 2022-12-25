package com.itheima.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.common.BaseContext;
import com.itheima.common.R;
import com.itheima.dto.OrdersDto;
import com.itheima.entity.*;
import com.itheima.exception.CustmException;
import com.itheima.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/order")
@Transactional
public class OrdersController {
    @Autowired
    private OrdersService ordersService;
    @Autowired
    private OrderDetailService orderDetailService;
    @Autowired
    private ShoppingCartService shoppingCartService;
    @Autowired
    private UserService userService;
    @Autowired
    private AddressBookService addressBookService;

    /**
     * 分页查询
     *
     * @param page
     * @param pageSize
     * @param number
     * @return
     */
    @GetMapping("/page")
    public R<IPage> pageR(int page, int pageSize, Long number, String beginTime, String endTime) {
        // 分页构造器
        IPage<Orders> pageinfo = new Page<>(page, pageSize);
        IPage<OrdersDto> pagedto = new Page<>(page, pageSize);
        // 创建构造器
        LambdaQueryWrapper<Orders> wrapper = new LambdaQueryWrapper<>();
        // 排序条件
        wrapper.orderByDesc(Orders::getOrderTime)
                // 查询条件  订单号
                .eq(number != null, Orders::getNumber, number)
                // 开始时间
                .gt(Strings.isNotEmpty(beginTime), Orders::getOrderTime, beginTime)
                // 结束时间
                .lt(Strings.isNotEmpty(endTime), Orders::getOrderTime, endTime);
        // 查询分页数据
        ordersService.page(pageinfo, wrapper);
        // 操作意思如上
        List<OrdersDto> dtoList = pageinfo.getRecords().stream().map((m) -> {
            // 创建dto对象
            OrdersDto dto = new OrdersDto();
            // 条件构造器
            LambdaQueryWrapper<OrderDetail> queryWrapper = new LambdaQueryWrapper<>();
            // 查询条件
            queryWrapper.eq(OrderDetail::getOrderId, m.getId());
            // 执行查询，返回集合
            List<OrderDetail> list = orderDetailService.list(queryWrapper);
            // 复制对象
            BeanUtils.copyProperties(m, dto);
            // 设置页面数据
            dto.setOrderDetails(list);
            return dto;
        }).collect(Collectors.toList());
        // 执行查询
        BeanUtils.copyProperties(pageinfo, pagedto, "record");
        pagedto.setRecords(dtoList);
        // 返回结果
        return R.success(pagedto);
    }

    /**
     * 历史订单分页查询
     *
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/userPage")
    public R<IPage> userPage(int page, int pageSize) {
        // 当前用户id
        Long userId = BaseContext.getThreadLocal();
        // 创建构造器
        Page<Orders> pageinfo = new Page<>(page, pageSize);
        // 创建构造器
        Page<OrdersDto> dtoPagee = new Page<>(page, pageSize);
        // 条件构造器
        LambdaQueryWrapper<Orders> wrapper = new LambdaQueryWrapper<>();
        // 根据用户id查询
        wrapper.eq(Orders::getId, userId);
        // 带哦用查询方法
        Page<Orders> ordersPage = ordersService.page(pageinfo, wrapper);
        // 遍历集合
        List<OrdersDto> list = ordersPage.getRecords().stream().map((m) -> {
            // 创建对象
            OrdersDto dto = new OrdersDto();
            // 条件构造器
            LambdaQueryWrapper<OrderDetail> queryWrapper = new LambdaQueryWrapper<>();
            // 根据ida查询
            queryWrapper.eq(OrderDetail::getDishId, m.getId());
            // 查询菜单将返回结果封装为集合
            List<OrderDetail> orderDetails = orderDetailService.list(queryWrapper);
            // 复制对象
            BeanUtils.copyProperties(m, dto);
            // 将页面数据赋值给dto对象
            dto.setOrderDetails(orderDetails);
            // 返回对象
            return dto;
        }).collect(Collectors.toList());
        // 将pageinfo数据属性复制，并排除页面数据
        BeanUtils.copyProperties(pageinfo, dtoPagee, "records");
        // 将页面数据复制给dto
        dtoPagee.setRecords(list);
        return R.success(dtoPagee);
    }

    /**
     * 用户下单
     *
     * @return
     */
    @PostMapping("/submit")
    public R<String> submitOrders(@RequestBody Orders orders) {
        // 获取当前用户id
        Long userId = BaseContext.getThreadLocal();
        // 查询当前用户的购物车数据
        LambdaQueryWrapper<ShoppingCart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShoppingCart::getUserId, userId);
        // 购物车数据实体类
        List<ShoppingCart> list = shoppingCartService.list(wrapper);
        // 如果购物车为空则抛一个业务异常
        if (list == null || list.size() == 0) {
            throw new CustmException("您的购物车还没有商品，请先去下订单吧^_^");
        }
        User user = userService.getById(userId);
        AddressBook addressBook = addressBookService.getById(orders.getAddressBookId());
        if (addressBook == null) {
            throw new CustmException("地址信息为空，请先填写收货地址^_^");
        }
        AtomicInteger atomicInteger = new AtomicInteger(0);
        long id = IdWorker.getId();
        // 向订单表插入数据，一条数据
       List<OrderDetail> orderDetails= list.stream().map((m)->{
           OrderDetail orderDetail = new OrderDetail();
           orderDetail.setId(m.getId());
           orderDetail.setOrderId(id);
           orderDetail.setNumber(m.getNumber());
           orderDetail.setDishFlavor(m.getDishFlavor());
           orderDetail.setDishId(m.getDishId());
           orderDetail.setSetmealId(m.getSetmealId());
           orderDetail.setName(m.getName());
           atomicInteger.addAndGet(m.getAmount().multiply(new BigDecimal(m.getNumber())).intValue());
           BeanUtils.copyProperties(m,orderDetail);
            return orderDetail;
       }).collect(Collectors.toList());
        orders.setNumber(String.valueOf(IdWorker.getId()));
        orders.setId(user.getId());
        orders.setId(id);
        orders.setCheckoutTime(LocalDateTime.now());
        orders.setStatus(2);
        orders.setAmount(new BigDecimal(atomicInteger.get()));//总金额
        orders.setUserId(userId);
        orders.setUserName(user.getName());
        orders.setConsignee(addressBook.getConsignee());
        orders.setPhone(addressBook.getPhone());
        orders.setAddress((addressBook.getProvinceName() == null ? "" : addressBook.getProvinceName())
                + (addressBook.getCityName() == null ? "" : addressBook.getCityName())
                + (addressBook.getDistrictName() == null ? "" : addressBook.getDistrictName())
                + (addressBook.getDetail() == null ? "" : addressBook.getDetail()));
        orders.setAmount(new BigDecimal(atomicInteger.get()));

        //像订单表插入数据
        ordersService.save(orders);
        // 向订单明细表里面插入数据，多条数据
        orderDetailService.saveBatch(orderDetails);
        // 清空购物车
        shoppingCartService.remove(wrapper);
        return R.success("下单成功");

    }
}
