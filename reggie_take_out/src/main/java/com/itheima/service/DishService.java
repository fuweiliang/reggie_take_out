package com.itheima.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.dto.DishDto;
import com.itheima.entity.Dish;

import java.util.List;

public interface DishService extends IService<Dish> {
    boolean saveFlavor(DishDto dishDto);
    DishDto geiByIdAndFlavor(Long ids);
    boolean alterDishAndFlavor(DishDto dishDto);
    boolean deleteByIds(List<Long> ids);
}
