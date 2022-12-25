package com.itheima.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.common.R;
import com.itheima.dto.SetmealDto;
import com.itheima.entity.Category;
import com.itheima.entity.Setmeal;
import com.itheima.entity.SetmealDish;
import com.itheima.service.CategoryService;
import com.itheima.service.SetmealDishService;
import com.itheima.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/setmeal")
@Transactional
public class SetmealDishController {
    @Autowired
    private SetmealDishService setmealDishService;
    @Autowired
    private SetmealService setmealService;
    @Autowired
    private CategoryService categoryService;

    /**
     * @param setmealDto
     * @return
     */
    @PostMapping
    public R<String> saveSetmeal(@RequestBody SetmealDto setmealDto) {
        if (setmealService.savesetmeal(setmealDto)) {
            return R.success("保存成功^_^");
        }
        return R.error("保存失败");
    }

    /**
     * 菜品数据回显
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<SetmealDto> selectSetmeal(@PathVariable Long id) {

        // 调用方法查询
        Setmeal setmeal = setmealService.getById(id);
        // 创建赋值对象
        SetmealDto dto = new SetmealDto();
        // 将对象属性复制到dto
        if (setmeal != null) {
            BeanUtils.copyProperties(setmeal, dto);
        } else {
            log.error("查询对象为空");
        }
        // 查询条件
        LambdaQueryWrapper<SetmealDish> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SetmealDish::getSetmealId, id);
        List<SetmealDish> list = setmealDishService.list(wrapper);
        // 将页面数据list封装到dto
        dto.setSetmealDishes(list);
        return R.success(dto);
    }

    /**
     * 菜品分页查询
     *
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/page")
    public R<IPage<SetmealDto>> pageR(int page, int pageSize, String name) {
        // 构造分页gouzaoqi
        IPage<Setmeal> dishPage = new Page<>(page, pageSize);
        IPage<SetmealDto> dtoPage = new Page<>();
        // 构造条件构造器
        LambdaQueryWrapper<Setmeal> wrapper = new LambdaQueryWrapper<>();
        // 如果name的值不为空则执行此条件
        wrapper.like(Strings.isNotEmpty(name), Setmeal::getName, name)
                // 进行排序
                .orderByDesc(Setmeal::getUpdateTime);
        // 执行分页查询
        setmealService.page(dishPage, wrapper);
        // 用spring的方法将对象属性复制过去,其实就是拷贝除了records页面数据之外的都拷贝过去
        BeanUtils.copyProperties(dishPage, dtoPage, "records");
        // records其实就是页面的数据
        List<Setmeal> records = dishPage.getRecords();
        // 接收对象并封装
        List<SetmealDto> list = records.stream().map((m) -> {
            // 创建一个dto对象
            SetmealDto setmealDto = new SetmealDto();
            // 将页面数据和分页数据复制过去
            BeanUtils.copyProperties(m, setmealDto);
            // 调用category业务根据records穿过来的id进行查询
            Category category = categoryService.getById(m.getCategoryId());

            // 将查询到的·菜品名字进行赋值
            setmealDto.setCategoryName(category.getName());

            // 返回对象
            return setmealDto;
        }).collect(Collectors.toList());
        // 将页面数据写入
        dtoPage.setRecords(list);

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
        setmealService.deleteWithDish(ids);
        return R.success("删除菜品成功");
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
    public R<String> Status(@PathVariable Integer status, @RequestParam List<Long> ids) {
        LambdaUpdateWrapper<Setmeal> wrapper = new LambdaUpdateWrapper<>();
        wrapper.in(Setmeal::getId, ids).set(Setmeal::getStatus, status);
        if (setmealService.update(wrapper)) {
            return R.success("修改状态成功^_^");

        }
        return R.error("失败");
    }

    /**
     * 跟新菜品
     * @param setmealDto
     * @return
     */
    @PutMapping
    public R<Setmeal> updateWithDish(@RequestBody SetmealDto setmealDto) {
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        Long setmealId = setmealDto.getId();
        // 先根据id把setmealDish表中对应套餐的数据删了
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId, setmealId);
        setmealDishService.remove(queryWrapper);
        // 然后在重新添加
        setmealDishes = setmealDishes.stream().map((item) -> {
            // 这属性没有，需要我们手动设置一下
            item.setSetmealId(setmealId);
            return item;
        }).collect(Collectors.toList());
        // 更新套餐数据
        setmealService.updateById(setmealDto);
        // 更新套餐对应菜品数据
        setmealDishService.saveBatch(setmealDishes);
        return R.success(setmealDto);
    }

    /**
     *获取菜品分类对应的套餐
     * @param setmeal
     * @return
     */
    @GetMapping("/list")
    public R<List<Setmeal>> list(Setmeal setmeal){
        LambdaQueryWrapper<Setmeal> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(setmeal.getStatus()!=null,Setmeal::getStatus,1)
                .eq(setmeal.getCategoryId()!=null,Setmeal::getCategoryId,setmeal.getCategoryId());
        List<Setmeal> list = setmealService.list(wrapper);
        return R.success(list);
    }
}
