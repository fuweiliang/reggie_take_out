package com.itheima.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.common.BaseContext;
import com.itheima.common.R;
import com.itheima.entity.ShoppingCart;
import com.itheima.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Slf4j
@RestController
@RequestMapping("/shoppingCart")
@Transactional
public class ShoppingCartController {
    @Autowired
    private ShoppingCartService shoppingCartService;

    /**
     * 添加到购物车
     *
     * @param shoppingCart
     * @return
     */
    @PostMapping("/add")
    public R<ShoppingCart> add(ShoppingCart shoppingCart) {
        // 设置用户id，指定当前那个用户的购物车数据
        Long userId = BaseContext.getThreadLocal();
        shoppingCart.setUserId(userId);
        // 查询当前菜品或这套餐是否在购物车中
        Long dishId = shoppingCart.getDishId();
        // 如果已经存在，就在原来的基础上加一
        LambdaQueryWrapper<ShoppingCart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(dishId != null, ShoppingCart::getDishId, dishId)
                .eq(dishId == null, ShoppingCart::getSetmealId, shoppingCart.getSetmealId());
        ShoppingCart cart = shoppingCartService.getOne(wrapper);

        if (cart != null) {
            // 如果已经存在就在原来的数量赏面加一
            shoppingCart.setNumber(cart.getNumber() + 1);
            shoppingCartService.updateById(cart);
        } else {
            shoppingCart.setNumber(1);
            shoppingCartService.save(shoppingCart);
        }

        return R.success(cart);
    }

    /**
     * 查看购物车
     *
     * @return
     */
    @GetMapping("/list")
    public R<List<ShoppingCart>> getAllForShoppingCart() {
        LambdaQueryWrapper<ShoppingCart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShoppingCart::getUserId, BaseContext.getThreadLocal());
        List<ShoppingCart> list = shoppingCartService.list(wrapper);
        return R.success(list);
    }

    /**
     * 购物车删减商品
     * 根据菜品或则和套餐id分别删除
     *
     * @param shoppingCart
     * @return
     */
    @PostMapping("/sub")
    public R<ShoppingCart> deleteCart(ShoppingCart shoppingCart) {
        // 条件构造器
        LambdaQueryWrapper<ShoppingCart> wrapper = new LambdaQueryWrapper<>();
        // 如果不为空则执行查询，将用户线程id作为查询id
        wrapper.eq(ShoppingCart::getUserId, BaseContext.getThreadLocal())
                // 如果菜品id不为空则执行此查询
                .eq(shoppingCart.getDishId() != null, ShoppingCart::getDishId, shoppingCart.getDishId())
                // 如果套餐id部位空则执行此查询
                .eq(shoppingCart.getSetmealId() != null, ShoppingCart::getSetmealId, shoppingCart.getSetmealId());
        // 查询购物车中已有的数据
        ShoppingCart cart = shoppingCartService.getOne(wrapper);
        // 获取订单数据量
        cart.setNumber(cart.getNumber() - 1);
        // 如果相同订单大于0则更新，小于0则是删除
        if (cart.getNumber() > 0) {
            shoppingCartService.updateById(cart);
        } else if (cart.getNumber() == 0) {
            shoppingCartService.removeById(cart);
        } else {
            return R.error("删除失败,系统繁忙，请稍后重试!_!");
        }
        return R.success(cart);
    }

    /**
     * 清除购物车中已有的商品
     *
     * @return
     */
    @DeleteMapping("/clean")
    public R<String> deleteAll() {
        LambdaQueryWrapper<ShoppingCart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BaseContext.getThreadLocal() != null, ShoppingCart::getId, BaseContext.getThreadLocal());
        shoppingCartService.remove(wrapper);
        log.info("清空购物车成功^_^");
        return R.success("清空购物车成功^_^");
    }
}
