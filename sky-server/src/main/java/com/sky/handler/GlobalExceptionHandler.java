package com.sky.handler;

import com.sky.constant.MessageConstant;
import com.sky.exception.BaseException;
import com.sky.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * 全局异常处理器，处理项目中抛出的业务异常
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 捕获业务异常
     * @param ex
     * @return
     */
    @ExceptionHandler
    public Result exceptionHandler(BaseException ex){
        log.error("异常信息：{}", ex.getMessage());
        return Result.error(ex.getMessage());
    }

    /**
     * 处理新增用户时因 “用户名重复” 而抛出的异常
     * java.sql.SQLIntegrityConstraintViolationException: Duplicate entry 'zhangsan' for key 'employee.idx_username'
     *
     * @return
     */
    @ExceptionHandler
    public Result exceptionHandler(SQLIntegrityConstraintViolationException exception) {
        String msg = exception.getMessage();
        //抛出这个异常时，可能会有多种原因，因此要进行针对性的处理
        //用户名重复：Duplicate entry 'zhangsan' for key 'employee.idx_username'
        if(msg.startsWith("Duplicate entry")) {
            //当且仅当异常信息中存在 Duplicate entry 时，才将重复的用户名提取出来，作为有效信息提交给前端
            msg = msg.substring("Duplicate entry '".length());
            msg = msg.substring(0, msg.indexOf("'"));
            return Result.error(msg + MessageConstant.USER_ALREADY_EXITS);
        }
        //如果没有匹配到任何错误，那么提示 “未知错误”
        return Result.error("未知错误");
    }

}
