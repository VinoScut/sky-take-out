package com.sky.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sky.constant.MessageConstant;
import com.sky.constant.PasswordConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.entity.Employee;
import com.sky.exception.AccountLockedException;
import com.sky.exception.AccountNotFoundException;
import com.sky.exception.PasswordErrorException;
import com.sky.mapper.EmployeeMapper;
import com.sky.service.EmployeeService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;

@Service
public class EmployeeServiceImpl extends ServiceImpl<EmployeeMapper, Employee> implements EmployeeService {

    @Autowired
    private EmployeeMapper employeeMapper;

    private LambdaUpdateWrapper<Employee> employeeLambdaUpdateWrapper = new LambdaUpdateWrapper<>();

    private LambdaQueryWrapper<Employee> employeeLambdaQueryWrapper = new LambdaQueryWrapper<>();

    @Override
    public void save(EmployeeDTO employeeDTO) {
        //在调用 DAO层 操作数据库之前，先将 EmployeeDTO 转换为 Employee，因为 Employee 中的所有字段与数据库中的字段是 “完全对应” 的
        Employee employee = new Employee();
        //对于 EmployeeDTO 和 Employee 中 “同名、同类型” 的属性来说，可以直接借助 Spring 的 BeanUtils.copyProperties 来拷贝属性
        BeanUtils.copyProperties(employeeDTO, employee);
        //除此之外，还需要设置 Employee 的其他属性，因为存入数据库时，要对所有字段进行赋值
        //(1)创建时间和修改时间，创建视为“第一次修改操作”，因此二者都取当下的时间
        LocalDateTime now = LocalDateTime.now();
        employee.setCreateTime(now);
        employee.setUpdateTime(now);
        //(2)密码取默认值123456，注意：用 “常量” 代替字符串，先使用 “md5加密” 后再存入数据库
        employee.setPassword(DigestUtils.md5DigestAsHex(PasswordConstant.DEFAULT_PASSWORD.getBytes()));
        //(3)员工状态：默认是启用的，同样用 “常量” 代替写死的字符串，起到 “见名知意” 的效果
        employee.setStatus(StatusConstant.ENABLE);
        //(4)新增者和修改者的 id：调用 BaseContext，从当前线程的 ThreadLocalMap 中取出存储的 empId
        //由于程序一启动并且调用 BaseContext 后，其中的 ThreadLocal 就会被创建并且保持不变，所以每个线程都能取到自己的 empId
        Long empId = BaseContext.getCurrentId();
        employee.setCreateUser(empId);
        employee.setUpdateUser(empId);
        //使用 employeeMapper 传入封装好的 Employee 完成新增操作
        employeeMapper.insert(employee);
    }

    /**
     * 员工登录
     *
     * @param employeeLoginDTO
     * @return
     */
    public Employee login(EmployeeLoginDTO employeeLoginDTO) {
        String username = employeeLoginDTO.getUsername();
        String password = employeeLoginDTO.getPassword();

        //1、根据用户名查询数据库中的数据
        Employee employee = employeeMapper.getByUsername(username);

        //2、处理各种异常情况（用户名不存在、密码不对、账号被锁定）
        if (employee == null) {
            //账号不存在
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        //密码比对
        //将从前端获取到的密码先进行 md5 加密，然后再与数据库中的密码进行比对 (因为数据库中的密码全部经过 md5 加密，所以比对时也要先加密)
        //将明文密码通过 md5 转换为16进制的字符串，然后与数据库中的数据进行比对
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        if (!password.equals(employee.getPassword())) {
            //密码错误
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        if (employee.getStatus() == StatusConstant.DISABLE) {
            //账号被锁定
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        //3、返回实体对象
        return employee;
    }



}
