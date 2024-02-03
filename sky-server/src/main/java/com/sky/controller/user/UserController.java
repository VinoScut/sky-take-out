package com.sky.controller.user;


import com.sky.constant.JwtClaimsConstant;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.properties.JwtProperties;
import com.sky.result.Result;
import com.sky.service.user.UserService;
import com.sky.utils.JwtUtil;
import com.sky.vo.UserLoginVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/user/user")
@Api(tags = "C端用户接口")
@Slf4j
public class UserController {

    @Autowired
    JwtProperties jwtProperties;
    @Autowired
    UserService userService;

    @ApiOperation("微信登录")
    @PostMapping("/login")
    public Result<UserLoginVO> userLogin(@RequestBody UserLoginDTO userLoginDTO) {
        //得到当前用户对应的 User 对象
        User user = userService.wxLogin(userLoginDTO);
        Map<String, Object> claims = new HashMap<>();
        //将当前用户的 id(主键值) 放入 jwt令牌 中
        claims.put(JwtClaimsConstant.USER_ID, user.getId());
        String token = JwtUtil.createJWT(jwtProperties.getUserSecretKey(), jwtProperties.getUserTtl(), claims);
        //由 token 和 openid 构建出 UserLoginVO 对象并返回
        UserLoginVO userLoginVO = UserLoginVO.builder()
                                            .id(user.getId())
                                            .openid(user.getOpenid())
                                            .token(token)
                                            .build();
        return Result.success(userLoginVO);
    }
}
