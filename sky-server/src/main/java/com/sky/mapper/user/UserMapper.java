package com.sky.mapper.user;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sky.entity.User;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UserMapper extends BaseMapper<User> {

    @Select("select id, openid, create_time from user where openid = #{openid}")
    User getUserByOpenId(String openid);

    @Select("select name from user where id = #{userId}")
    String getUserNameById(Long userId);

    @Select("select id, openid, name, create_time from user where create_time between #{beginTime} and #{endTime}")
    List<User> getUserBetween(LocalDateTime beginTime, LocalDateTime endTime);

    @Select("select count(*) from user where create_time < #{beginTime}")
    Integer getTotalUserCountBefore(LocalDateTime beginTime);
}
