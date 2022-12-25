package com.itheima.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.dto.SetmealDto;
import com.itheima.entity.Setmeal;

import java.util.List;

public interface SetmealService extends IService<Setmeal> {
    boolean savesetmeal(SetmealDto setmealDto);
    void deleteWithDish(List<Long> ids);
}
