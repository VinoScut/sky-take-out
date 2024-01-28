package com.sky.controller.admin;

import com.sky.constant.JwtClaimsConstant;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.properties.JwtProperties;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.admin.EmployeeService;
import com.sky.utils.JwtUtil;
import com.sky.vo.EmployeeLoginVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 员工管理
 */
@RestController
@RequestMapping("/admin/employee")
@Slf4j
@Api(tags = "员工相关接口")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 登录
     *
     * @param employeeLoginDTO
     * @return
     */
    @PostMapping("/login")
    @ApiOperation("员工登入")
    public Result<EmployeeLoginVO> login(@RequestBody EmployeeLoginDTO employeeLoginDTO) {
        log.info("员工登录：{}", employeeLoginDTO);

        Employee employee = employeeService.login(employeeLoginDTO);

        //登录成功后，生成jwt令牌
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.EMP_ID, employee.getId());
        String token = JwtUtil.createJWT(
                jwtProperties.getAdminSecretKey(),
                jwtProperties.getAdminTtl(),
                claims);

        EmployeeLoginVO employeeLoginVO = EmployeeLoginVO.builder()
                .id(employee.getId())
                .userName(employee.getUsername())
                .name(employee.getName())
                .token(token)
                .build();

        return Result.success(employeeLoginVO);
    }

    /**
     * 退出
     *
     * @return
     */
    @PostMapping("/logout")
    @ApiOperation("员工登出")
    public Result<String> logout() {
        return Result.success();
    }

    @PostMapping
    @ApiOperation("新增员工")
    public Result save(@RequestBody EmployeeDTO employeeDTO) {
        log.info("新增员工：{}", employeeDTO);
        employeeService.save(employeeDTO);
        return Result.success();
    }

    /**
     * 员工的分页查询
     * @param employeePageQueryDTO 在分页查询时，将前端传过来的信息封装为一个 DTO，再传给 service 层
     * @return
     */
    @GetMapping("/page")
    @ApiOperation("员工分页查询")
    public Result<PageResult<Employee>> page(EmployeePageQueryDTO employeePageQueryDTO) {
        log.info("员工分页查询，参数为：{}", employeePageQueryDTO);
        PageResult<Employee> pageResult = employeeService.page(employeePageQueryDTO.getName(), employeePageQueryDTO.getPage(), employeePageQueryDTO.getPageSize());
        return Result.success(pageResult);
    }

    /**
     * 启用、禁用员工账号
     * @param id 员工id
     * @param status 修改后的状态
     * @return
     */
    @PostMapping("/status/{status}")
    @ApiOperation("启用/禁用员工账号")
    public Result enableOrDisableEmployee(@RequestParam Long id, @PathVariable("status") Integer status) {
        log.info("启用禁用员工账号：{}, {}", id, status);
        Long currentId = BaseContext.getCurrentId();
        //校验当前用户是否是管理员，如果不是，不允许执行此操作
        if(currentId != 1) {
            return Result.error(MessageConstant.AINT_ADMIN);
        }
        employeeService.changeEmployeeStatus(id, status);
        return Result.success("操作成功");
    }

    /**
     * 根据 id 查询对应的员工信息
     * @param id 员工 id
     * @return
     */
    @GetMapping("/{id}")
    @ApiOperation("根据id查询员工信息")
    public Result<Employee> getEmployeeById(@PathVariable("id") Integer id){
        Employee employee = employeeService.getEmployeeById(id);
        //查询到的 employee 对象中包含密码，虽然密码已经是经过“加密”的，但仍然可以在后端处理一下，不让密码相关的内容暴露在前端页面
        employee.setPassword("******");
        return Result.success(employee);
    }

    /**
     * 编辑员工的信息
     * @return
     */
    @PutMapping
    @ApiOperation("编辑员工信息")
    public Result editEmployee(@RequestBody EmployeeDTO employeeDTO) {
        if(BaseContext.getCurrentId() != 1) {
            return Result.error(MessageConstant.AINT_ADMIN);
        }
        employeeService.editEmployee(employeeDTO);
        return Result.success();
    }
}
