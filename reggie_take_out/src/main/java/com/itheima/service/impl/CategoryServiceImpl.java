package com.itheima.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.common.R;
import com.itheima.entity.Category;
import com.itheima.entity.Dish;
import com.itheima.entity.Setmeal;
import com.itheima.mapper.CategoryMapper;
import com.itheima.service.CategoryService;
import com.itheima.service.DishService;
import com.itheima.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author fu'wei'liang
 */
@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {
    @Autowired
    private DishService dishService;
    @Autowired
    private SetmealService setmealService;

    @Override
    public R<String> remove(Long ids) {
        System.out.println(ids);
        LambdaQueryWrapper<Dish> wrapper = new LambdaQueryWrapper<>();
        // 添加查询条件，根据分类id进行查询
        wrapper.eq(Dish::getId, ids);
        // 如果插叙你反悔书大于一则表示菜单已有关联不能删除，则结果反馈给用户
        if (dishService.count(wrapper) > 0) {
            throw new RuntimeException("删除失败，该菜品已有关联^_^!");
        }
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Setmeal::getId, ids);
        if (setmealService.count(queryWrapper) > 0) {
            throw new RuntimeException("删除失败，该菜品已有关联^_^!");
        }
        if (super.removeById(ids)) {
            // 查询没有任何关联可以正常删除
            return R.success("删除菜品成功^_^");

        }
        return R.error("操作失败");
    }
}
