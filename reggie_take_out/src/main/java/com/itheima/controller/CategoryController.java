package com.itheima.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.common.R;
import com.itheima.entity.Category;
import com.itheima.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author fu'wei'liang
 */
@Slf4j
@RestController
@RequestMapping("/category")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    /**
     * 菜品分页查询
     *
     * @param page
     * @param pageSize
     * @return
     */

    @GetMapping("/page")
    public R<IPage<Category>> page(int page, int pageSize) {
        // 构造分页构造器
        IPage<Category> objectPage = new Page<>(page, pageSize);
        // 执行查询
        IPage<Category> categoryPage = categoryService.page(objectPage);
        if (categoryPage == null) {
            return R.error("分页查询失败");
        }
        return R.success(objectPage);
    }

    /**
     * 根据菜品id删除
     *
     * @param ids
     * @return
     */

    @DeleteMapping
    public R<String> deleteByIds(Long ids) {
        return categoryService.remove(ids);
    }

    /**
     * 修改菜品
     *
     * @param request
     * @param category
     * @return
     */
    @PutMapping
    public R<String> edit(HttpServletRequest request, @RequestBody Category category) {
        Long cat = (Long) request.getSession().getAttribute("employee");
        // 将操作用户id记录到数据库
        category.setUpdateUser(cat);
        if (categoryService.updateById(category)) {

            return R.success("修改菜品成功^_^");
        }

        return R.error("修改菜品失败^_^!");
    }

    /**
     * 新增菜品
     *
     * @param request
     * @param category
     * @return
     */
    @PostMapping
    public R<String> save(HttpServletRequest request, @RequestBody Category category) {
        Long cat = (Long) request.getSession().getAttribute("employee");
        // 将操作用户id记录到数据库
        category.setUpdateUser(cat);

        category.setCreateUser(cat);
        if (categoryService.save(category)) {
            return R.success("新增菜品成功^_^");
        }
        return R.error("新增菜品失败^_^!");
    }

    /**
     * 根据条件查询分类
     *
     * @param category
     * @return
     */
    @GetMapping("/list")
    public R<List<Category>> listR(Category category) {
        // 条件构造器
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        //判断如果为空则不执行
        wrapper.eq(category.getType()!=null, Category::getType, category.getType());
        //根据排序和更新时间进行排序
        wrapper.orderByAsc(Category::getSort).orderByDesc(Category::getUpdateTime);
        //传入查询
        List<Category> list = categoryService.list(wrapper);
        //返回结果
        return R.success(list);
    }

}
