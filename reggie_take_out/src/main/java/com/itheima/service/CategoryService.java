package com.itheima.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.common.R;
import com.itheima.entity.Category;

public interface CategoryService extends IService<Category> {
    R<String> remove(Long ids);
}
