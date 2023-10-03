package com.sky.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sky.entity.Employee;
import org.apache.ibatis.annotations.Select;

public interface EmployeeMapper extends BaseMapper<Employee> {

    /**
     * 根据用户名查询员工
     * @param username
     * @return
     */
    @Select("select * from employee where username = #{username} and status = 1")
    Employee getByUsername(String username);

    /**
     * 通用的 “修改员工” 的方法，根据传入的 Employee 对象，对指定的员工记录进行修改
     * @param employee
     */
    void updateEmployee(Employee employee);

}
