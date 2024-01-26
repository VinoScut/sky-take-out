package com.sky.aspect;

import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

@Slf4j
@Aspect //将当前类标记为一个 “切面类”
@Component //将切面类交给 Spring 进行管理
public class AutoFillAspect {

    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    public void autoFillPointCut() {
    }

    @Before("autoFillPointCut()")
    public void autoFill(JoinPoint joinPoint) {
        //从 “切入点”joinPoint 得到签名，由于我们需要获取方法上的“注解信息”，所以需要向下转型为 MethodSignature，否则没有相应的方法
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        AutoFill autoFill = signature.getMethod().getAnnotation(AutoFill.class);
        //获取到注解信息后，判断 AutoFill 上面的 value() 是 INSERT 还是 UPDATE
        OperationType operationType = autoFill.value();
        //获取方法参数，只获取第一个参数，因此相应实体类对象必须放置在第一个位置，否则自动填充就会出错
        Object obj = joinPoint.getArgs()[0];
        Class<?> clazz = obj.getClass();
        LocalDateTime now = LocalDateTime.now();
        Long currentId = BaseContext.getCurrentId();
        try {
            //注意：这里使用 “反射” 为对象属性进行赋值时，总的来说有2种方法
            //(1)获取属性，为属性赋值 —— 一般来说，属性都是 private，因此获取后还需要进行反射爆破，多了一步 .setAccessible() 的操作
            //(2)获取 set方法，使用 .setXXX() 进行赋值 —— .setXXX() 一般都是 public 的方法，获取之后直接调用，不需要反射爆破，更方便
            if (operationType == OperationType.INSERT) {
                Method setCreateTime = clazz.getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
                setCreateTime.invoke(obj, now);
                Method setCreateUser = clazz.getDeclaredMethod(AutoFillConstant.SET_CREATE_USER, Long.class);
                setCreateUser.invoke(obj, currentId);
            }
            Method setUpdateTime = clazz.getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
            setUpdateTime.invoke(obj, now);
            Method setUpdateUser = clazz.getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);
            setUpdateUser.invoke(obj, currentId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
