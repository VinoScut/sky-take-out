package com.sky.service.user.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sky.constant.MessageConstant;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.exception.LoginFailedException;
import com.sky.mapper.user.UserMapper;
import com.sky.properties.WeChatProperties;
import com.sky.service.user.UserService;
import com.sky.utils.HttpClientUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    WeChatProperties weChatProperties;
    @Autowired
    UserMapper userMapper;

    static final String WX_LOGIN = "https://api.weixin.qq.com/sns/jscode2session";

    /**
     * 由得到的授权码 code 请求微信接口，获取当前用户的 openid，对于新用户，将其数据添加到数据库中
     * @param userLoginDTO 用户登录DTO
     * @return 当前用户对应的 User 对象
     */
    @Override
    public User wxLogin(UserLoginDTO userLoginDTO) {
        //构建 GET 请求的请求参数
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("appid", weChatProperties.getAppid());
        paramMap.put("secret", weChatProperties.getSecret());
        paramMap.put("js_code", userLoginDTO.getCode());
        paramMap.put("grant_type", "authorization_code");
        //使用 HttpClient 请求微信接口，得到返回 Entity 对应的 json 字符串
        String jsonBody = HttpClientUtil.doGet(WX_LOGIN, paramMap);
        //将这个 json字符串 解析为 JsonObject 对象，方便我们从中获取数据
        JSONObject jsonObject = JSON.parseObject(jsonBody);
        //获取当前用户的 openid
        String openid = (String)jsonObject.get("openid");
        //如果获取到的 openid 非法，抛出异常
        if(!StringUtils.hasText(openid)) {
            throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
        }
        //如果获取到了 openid，去数据库中查询当前用户是否已存在，如果是新用户，那么将其添加到数据库中
        User user = userMapper.getUserByOpenId(openid);
        if(user == null) {
            //创建当前用户的 User 对象，只为其中的 openid/createTime 赋值，其余属性待用户在 “个人中心” 完善后再赋值
            user = User.builder()
                        .openid(openid)
                        .createTime(LocalDateTime.now())
                        .build();
            userMapper.insert(user);
        }
        return user;
    }
}















