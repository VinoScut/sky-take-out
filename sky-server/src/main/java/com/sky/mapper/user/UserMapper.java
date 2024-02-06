package com.sky.mapper.user;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sky.entity.User;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

@Repository
public interface UserMapper extends BaseMapper<User> {

    @Select("select id, openid, create_time from user where openid = #{openid}")
    User getUserByOpenId(String openid);

    @Select("select name from user where id = #{userId}")
    String getUserNameById(Long userId);

}
