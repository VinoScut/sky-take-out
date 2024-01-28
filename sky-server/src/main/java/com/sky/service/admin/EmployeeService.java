package com.sky.service.admin;

import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.entity.Employee;
import com.sky.result.PageResult;

public interface EmployeeService {
    /**
     * 新增员工
     * @param employeeDTO
     */
    void save(EmployeeDTO employeeDTO);

    /**
     * 员工登录
     * @param employeeLoginDTO
     * @return
     */
    Employee login(EmployeeLoginDTO employeeLoginDTO);

    PageResult<Employee> page(String name, Integer page, Integer pageSize);

    void changeEmployeeStatus(Long id, Integer status);

    Employee getEmployeeById(Integer id);

    void editEmployee(EmployeeDTO employeeDTO);
}
