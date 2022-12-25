package com.itheima.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.dto.DishDto;
import com.itheima.entity.Dish;
import com.itheima.entity.DishFlavor;
import com.itheima.exception.CustmException;
import com.itheima.mapper.DishMapper;
import com.itheima.service.DishFlavorService;
import com.itheima.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {
    @Autowired
    private DishFlavorService dishFlavorService;

    @Override
    public boolean saveFlavor(DishDto dishDto) {
        // 将对象保存
        this.save(dishDto);
        // 根据id查询
        Long id = dishDto.getId();
        // 带哦用dtolist封装集合
        List<DishFlavor> flavors = dishDto.getFlavors();

        // 遍历将id存入
        flavors.forEach((m) -> {
            m.setDishId(id);
        });
        // 将菜品口味表保存
        if (dishFlavorService.saveBatch(dishDto.getFlavors())) {
            log.info("业务层，菜品保存成功");
            return true;
        }
        return false;
    }

    @Override
    public DishDto geiByIdAndFlavor(Long ids) {
        // 调用方法查询
        Dish dish = this.getById(ids);
        // 创建赋值对象
        DishDto dto = new DishDto();
        // 将对象属性赋值到dto
        BeanUtils.copyProperties(dish, dto);
        // 查询条件
        LambdaQueryWrapper<DishFlavor> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DishFlavor::getId, dish.getId());
        // 调用业务并将数据封装到list
        List<DishFlavor> list = dishFlavorService.list(wrapper);
        // 将list封装到dto
        dto.setFlavors(list);
        return dto;
    }

    /**
     * 修改菜品口味和菜品
     *
     * @param dishDto
     * @return
     */
    @Override
    public boolean alterDishAndFlavor(DishDto dishDto) {
        // 更新dish表的信息
        this.updateById(dishDto);
        // 清理当前菜品的口味数据，dish--flavor表的delete操作
        LambdaQueryWrapper<DishFlavor> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DishFlavor::getDishId, dishDto.getId());
        if (dishFlavorService.remove(wrapper)) {

        }
        // 添加当前提交过来的口味数据
        List<DishFlavor> flavors = dishDto.getFlavors();
        // 如果提示重复id这里可以利用MP的雪花算法重新设置 m.setid(IdWorkr.getId);
        flavors = flavors.stream().map((m) -> {
            m.setDishId(dishDto.getId());
            return m;
        }).collect(Collectors.toList());
        dishFlavorService.saveBatch(flavors);

        return true;
    }

    @Override
    public boolean deleteByIds(List<Long> ids) {
        LambdaQueryWrapper<Dish> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Dish::getStatus, 1).in(Dish::getId, ids);
        if ((this.count(wrapper)) > 0) {
            throw new CustmException("当前套餐正在售卖中，请您先停止售卖再次重试!");
        }
        // 如果不是正在售卖的套餐，则可以删除
        this.removeByIds(ids);
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Dish::getId, ids);
        this.remove(queryWrapper);
        return true;
    }
}
