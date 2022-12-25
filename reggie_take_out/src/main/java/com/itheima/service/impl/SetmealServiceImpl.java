package com.itheima.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.dto.SetmealDto;
import com.itheima.entity.Setmeal;
import com.itheima.entity.SetmealDish;
import com.itheima.exception.CustmException;
import com.itheima.mapper.SetmealMapper;
import com.itheima.service.SetmealDishService;
import com.itheima.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author fu'wei'liang
 */
@Service
@Transactional// 平台事务管理器
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {
    @Autowired
    private SetmealDishService setmealDishService;


    /**
     * 新增套餐，同时保存套餐和菜品的关联关系
     *
     * @param setmealDto
     * @return
     */
    @Override
    public boolean savesetmeal(SetmealDto setmealDto) {

        // 保存套餐的基本信息，操作setmeal,执行insert操作
        this.save(setmealDto);
        // 保存套餐和菜品的关联关系，操作setmeal_dish,执行insert操作
        List<SetmealDish> dishes = setmealDto.getSetmealDishes();
        dishes = dishes.stream().map((m) -> {
            m.setSetmealId(setmealDto.getId());
            return m;
        }).collect(Collectors.toList());
        // 将表的关系存入
        setmealDishService.saveOrUpdateBatch(dishes);

        return true;
    }


    @Override
    public void deleteWithDish(List<Long> ids) {
        LambdaQueryWrapper<Setmeal> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Setmeal::getStatus, 1).in(Setmeal::getId, ids);
        // 查询当当前受阿门i的套餐是否有
        if ((this.count(wrapper)) > 0) {
            throw new CustmException("当前套餐正在售卖中，请先停止售卖在进行删除");
        }
        // 如果不是在售卖的套餐可以删除
        this.removeByIds(ids);
        // 继续删除关系表
        LambdaQueryWrapper<SetmealDish> setmealDishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        setmealDishLambdaQueryWrapper.in(SetmealDish::getSetmealId, ids);
        setmealDishService.remove(setmealDishLambdaQueryWrapper);
    }
}
