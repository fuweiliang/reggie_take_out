package com.itheima.common;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 元数据处理器
 */
@Slf4j
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {
    @Override
    public void insertFill(MetaObject metaObject) {
        log.info("insertFill字段填充");
        // 统一为所有该插入字段赋值
        metaObject.setValue("createTime", LocalDateTime.now());
        metaObject.setValue("updateTime", LocalDateTime.now());
        //将线程id传入
        metaObject.setValue("createUser", BaseContext.getThreadLocal());
        metaObject.setValue("updateUser", BaseContext.getThreadLocal());

    }

    @Override
    public void updateFill(MetaObject metaObject) {
        log.info("updateFill字段填充");
        // 统一为所有该更新字段赋值
        metaObject.setValue("createTime", LocalDateTime.now());
        metaObject.setValue("updateTime", LocalDateTime.now());
        //将线程id传入
        metaObject.setValue("createUser", BaseContext.getThreadLocal());
        metaObject.setValue("updateUser", BaseContext.getThreadLocal());
    }
}
