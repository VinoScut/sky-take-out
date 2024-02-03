package com.sky.interceptor;

import com.sky.constant.JwtClaimsConstant;
import com.sky.context.BaseContext;
import com.sky.properties.JwtProperties;
import com.sky.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 拦截用户端请求，校验其 token
 */
@Component
@Slf4j
public class JwtTokenUserInterceptor implements HandlerInterceptor {
    @Autowired
    JwtProperties jwtProperties;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //仅拦截业务方法，对于静态资源的请求，直接放行
        if(!(handler instanceof HandlerMethod)) {
            return true;
        }
        try {
            //从请求头中获取 token
            String token = request.getHeader(jwtProperties.getUserTokenName());
            log.info("用户端jwt校验：{}", token);
            //解析 token，拿到当前的用户id
            Claims claims = JwtUtil.parseJWT(jwtProperties.getUserSecretKey(), token);
            Long userId = Long.parseLong(claims.get(JwtClaimsConstant.USER_ID).toString());
            log.info("当前用户的 id 为：{}", userId);
            //注意：用户端仍然可以将 userId 存到 BaseContext 的 threadLocal对象 中
            //因为用户端的请求和客户端的请求属于 “不同的线程对象”，故底层的 ThreadLocalMap 也是“彼此独立”的，不受彼此的影响，不会发生覆盖
            BaseContext.setCurrentId(userId);
            return true;
        } catch (Exception e) {
            log.info("登录时出现异常，异常信息为：" + e.getMessage());
            //不抛出捕获到的异常，而是返回授权失败对应的http状态码，并拒绝放行当前请求
            response.setStatus(401);
            return false;
        }
    }
}













