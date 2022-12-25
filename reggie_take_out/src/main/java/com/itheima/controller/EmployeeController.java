package com.itheima.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.common.R;
import com.itheima.entity.Employee;
import com.itheima.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

/**
 * @author fu'wei'liang
 */
@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {
    @Autowired
    private EmployeeService employeeService;

    @PostMapping("/login")
    public R<Employee> login(HttpServletRequest request, @RequestBody Employee employee) {
        // 将管理员密码进行md5加密
        String password = employee.getPassword();

        password = DigestUtils.md5DigestAsHex(password.getBytes());
        // 2.根据页面提交的用户名username查询数据库

        LambdaQueryWrapper<Employee> wrapper = new LambdaQueryWrapper<>();

        wrapper.eq(Employee::getUsername, employee.getUsername());

        Employee user = employeeService.getOne(wrapper);
        // 3.如果没有查询到则返回登陆失败的结果
        if (user == null) {

            return R.error("用户名不存在^_^!");
        }
        // 4.密码比对，如果不一致则返回登录失败结果
        if (!user.getPassword().equals(password)) {
            return R.error("用户名或者密码错误^_^!");
        }
        // 5.查看员工状态，如果为已禁用状态，则返回员工已禁用结果
        if (user.getStatus() == 0) {
            return R.error("员工账号已禁用");
        }
        // 6.如果登录成功，将员工id存入session并返回登录结果
        request.getSession().setAttribute("employee", user.getId());
        return R.success(user);
    }

    /**
     * 登出
     *
     * @param request
     * @return
     */
    @PostMapping("/logout")
    public R<String> loginout(HttpServletRequest request) {
        // 清除浏览器中的session
        request.getSession().removeAttribute("employee");

        return R.success("登出成功^_^");
    }

    @PostMapping
    public R<String> save(HttpServletRequest request, @RequestBody Employee employee) {
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));

        Long emp = (Long) request.getSession().getAttribute("employee");

        employee.setCreateUser(emp);

        employee.setUpdateUser(emp);

        try {
            employeeService.save(employee);
        } catch (Exception e) {
            return R.error("新增员工失败,用户名重复^_^!");

        }

        return R.success("新增员工成功^_^");
    }

    /**
     * 员工分页查询
     *
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page<Employee>> page(int page, int pageSize, String name) {

        // 构造分页构造器
        Page<Employee> objectPage = new Page<>(page, pageSize);
        // 构造条件构造器
        LambdaQueryWrapper<Employee> wrapper = new LambdaQueryWrapper<>();
        // 如果name的值不为空则执行此条件
        wrapper.like(Strings.isNotEmpty(name), Employee::getName, name);
        // 如果是admin管理员账号则不显示
        wrapper.ne(Employee::getUsername, "admin");
        // 根据员工创建时间进行排序
        wrapper.orderByDesc(Employee::getCreateTime);

        // 执行查询
        Page<Employee> employeePage = employeeService.page(objectPage, wrapper);
        if (employeePage == null) {
         return    R.error("分页查询失败");
        }
        return R.success(objectPage);
    }

    /**
     * 修改员工状态
     *
     * @param employee
     * @return
     */
    @PutMapping
    public R<String> status(HttpServletRequest request, @RequestBody Employee employee) {
        Long emp = (Long) request.getSession().getAttribute("employee");

        employee.setUpdateUser(emp);

        if (employeeService.updateById(employee)) {
            return R.success("修改成功^_^");
        }
        return R.error("修改失败^_^!");

    }

    /**
     * 查询员工信息
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<Employee> alter(@PathVariable Long id) {

        Employee employee = employeeService.getById(id);
        if (employee == null) {
            return R.error("查询员工数据失败");
        }
        return R.success(employee);
    }


}
