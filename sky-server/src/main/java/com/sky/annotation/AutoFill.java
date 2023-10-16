package com.sky.annotation;

import com.sky.enumeration.OperationType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义注解：公共字段自动填充 <br/>
 * 需要进行属性自动填充的实体类对象，应该放置在参数列表的首位
 */
@Target(ElementType.METHOD) //当前注解只能用于 “方法” 上
@Retention(RetentionPolicy.RUNTIME) //注解保留至运行时期间，故在程序中可以通过 “反射” 来获取当前注解的信息
public @interface AutoFill {

    //标识当前方法的操作类型，是 INSERT 还是 UPDATE
    OperationType value();

}
