package com.sky.mp;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.sky.context.BaseContext;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

//当前类要交由 IOC容器 进行管理
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    //执行 “插入” 时的填充逻辑
    @Override
    public void insertFill(MetaObject metaObject) {
        LocalDateTime now = LocalDateTime.now();
        Long currentId = BaseContext.getCurrentId();
        //直接使用 metaObject 的 .setValue()方法，传入 “属性名” 和 “要赋的值”，对该对象的属性进行赋值
        metaObject.setValue("createTime", now);
        metaObject.setValue("createUser", currentId);
        metaObject.setValue("updateTime", now);
        metaObject.setValue("updateUser", currentId);
    }

    ////执行 “更新” 时的填充逻辑
    @Override
    public void updateFill(MetaObject metaObject) {
        LocalDateTime now = LocalDateTime.now();
        Long currentId = BaseContext.getCurrentId();
        metaObject.setValue("updateTime", now);
        metaObject.setValue("updateUser", currentId);
    }
}
