package com.itheima.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.common.R;
import com.itheima.dto.DishDto;
import com.itheima.entity.Category;
import com.itheima.entity.Dish;
import com.itheima.entity.DishFlavor;
import com.itheima.service.CategoryService;
import com.itheima.service.DishFlavorService;
import com.itheima.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author fu'wei'liang
 */
@Slf4j
@RestController
@RequestMapping("/dish")
@Transactional
public class DishController {
    @Autowired
    private DishService dishService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private DishFlavorService dishFlavorService;

    /**
     * 菜品管理
     *
     * @param dishDto
     * @return
     */
    @PostMapping
    public R<String> getALl(@RequestBody DishDto dishDto) {
        if (dishService.saveFlavor(dishDto)) {
            return R.success("新增菜品成功");
        }
        return R.error("新增菜品失败");
    }

    /**
     * 菜品分页查询
     *
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/page")
    public R<IPage<DishDto>> pageR(int page, int pageSize, String name) {
        // 构造分页gouzaoqi
        IPage<Dish> dishPage = new Page<>(page, pageSize);
        IPage<DishDto> dtoPage = new Page<>();
        // 构造条件构造器
        LambdaQueryWrapper<Dish> wrapper = new LambdaQueryWrapper<>();
        // 如果name的值不为空则执行此条件
        wrapper.like(Strings.isNotEmpty(name), Dish::getName, name)
                // 进行排序
                .orderByDesc(Dish::getSort);
        // 执行分页查询
        dishService.page(dishPage, wrapper);
        // 用spring的方法将对象属性复制过去,其实就是拷贝除了records页面数据之外的都拷贝过去
        BeanUtils.copyProperties(dishPage, dtoPage, "records");
        // records其实就是页面的数据
        List<Dish> records = dishPage.getRecords();
        // 接收对象并封装
        List<DishDto> list = records.stream().map((m) -> {
            // 创建一个dto对象
            DishDto dishDto = new DishDto();
            // 将页面数据和分页数据复制过去
            BeanUtils.copyProperties(m, dishDto);
            // 调用category业务根据records穿过来的id进行查询
            Category category = categoryService.getById(m.getCategoryId());
            if (category != null) {
                // 将查询到的·菜品名字进行赋值
                dishDto.setCategoryName(category.getName());
            }
            // 返回对象
            return dishDto;
        }).collect(Collectors.toList());
        // 将页面数据写入
        IPage<DishDto> dtoIPage = dtoPage.setRecords(list);
        if (dtoIPage == null) {
            return R.error("分页查询失败");
        }
        return R.success(dtoPage);
    }

    /**
     * 根据ids进行逻辑删除
     *
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> deleteByIds(@RequestParam List<Long> ids) {

        // 查询没有任何关联可以正常删除
        if (dishService.deleteByIds(ids)) {
            return R.success("删除菜品成功^_^");
        }
        return R.error("删除菜品失败^_^!");
    }

    /**
     * 菜品修改成功
     * 请求url里的查询参数通过@RequestParam注解可以和方法里的参数进行一一对应，
     * 比如现在有这么个url：/v2/banner?id=123&pos=3，
     * 那么接口的参数定义就得是@RequestParam Integer id,
     *
     * @param ids
     * @return
     * @RequestParam Integer pos,请求上面定义的接口看看返回值
     */
    @PostMapping("/status/{status}")
    public R<String> Status(@PathVariable Integer status, @RequestParam Long[] ids) {
        LambdaUpdateWrapper<Dish> wrapper = new LambdaUpdateWrapper<>();
        wrapper.in(Dish::getId, ids).set(Dish::getStatus, status);
        dishService.update(wrapper);
        return R.success("修改状态成功^_^");
    }


    /**
     * 查询菜品和对应的口味表
     *
     * @param ids
     * @return
     */
    @GetMapping("/{ids}")
    public R<DishDto> selectDish(@PathVariable Long ids) {
        DishDto dishDto = dishService.geiByIdAndFlavor(ids);
        return R.success(dishDto);
    }

    /**
     * 修改菜品
     *
     * @param dishDto
     * @return
     */
    @PutMapping
    public R<String> alterDish(@RequestBody DishDto dishDto) {
        if (dishService.alterDishAndFlavor(dishDto)) {
            return R.success("修改菜品和口味信息成功^_^");
        }
        return R.error("修改菜品和口味信息失败");
    }

    /**
     * 套餐管理
     *
     * @param categoryId
     * @return
     */
    @GetMapping("/list")
    public R<List<DishDto>> selectCateory(Dish dish) {
        // 条件查询器
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        // 根据传进来的categoryId查询
        queryWrapper.eq(dish.getCategoryId() != null, Dish::getCategoryId, dish.getCategoryId())
                // 只查询状态为1的菜品（在售菜品）
                .eq(Dish::getStatus, 1)
                // 简单排下序，其实也没啥太大作用
                .orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
        // 获取查询到的结果作为返回值
        List<Dish> list = dishService.list(queryWrapper);
        // item就是list中的每一条数据，相当于遍历了
        List<DishDto> dishDtoList = list.stream().map((item) -> {
            // 创建一个dishDto对象
            DishDto dishDto = new DishDto();
            // 将item的属性全都copy到dishDto里
            BeanUtils.copyProperties(item, dishDto);
            // 由于dish表中没有categoryName属性，只存了categoryId
            Long categoryId = item.getCategoryId();
            // 所以我们要根据categoryId查询对应的category
            Category category = categoryService.getById(categoryId);
            if (category != null) {
                // 然后取出categoryName，赋值给dishDto
                dishDto.setCategoryName(category.getName());
            }
            // 然后获取一下菜品id，根据菜品id去dishFlavor表中查询对应的口味，并赋值给dishDto
            Long itemId = item.getId();
            // 条件构造器
            LambdaQueryWrapper<DishFlavor> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            // 条件就是菜品id
            lambdaQueryWrapper.eq(itemId != null, DishFlavor::getDishId, itemId);
            // 根据菜品id，查询到菜品口味
            List<DishFlavor> flavors = dishFlavorService.list(lambdaQueryWrapper);
            // 赋给dishDto的对应属性
            dishDto.setFlavors(flavors);
            // 并将dishDto作为结果返回
            return dishDto;
            // 将所有返回结果收集起来，封装成List
        }).collect(Collectors.toList());
        return R.success(dishDtoList);
    }
}
