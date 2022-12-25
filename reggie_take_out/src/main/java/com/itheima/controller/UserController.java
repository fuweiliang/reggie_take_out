package com.itheima.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.itheima.common.R;
import com.itheima.entity.User;
import com.itheima.exception.CustmException;
import com.itheima.service.UserService;
import com.itheima.utils.QQMail;
import com.itheima.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.mail.MessagingException;
import javax.servlet.http.HttpSession;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;

    /**
     * 手机验证码发送
     *
     * @param user
     * @param session
     * @return
     */
    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user, HttpSession session) {
        try {
            // 获取用户手机号
            String phone = user.getPhone();
            // 生成水机五位验证码
            if (!phone.isEmpty()) {
                String code = QQMail.achieveCode();
                // 调用阿里云短信发送服务api
                // SMSUtils.sendMessage();
                log.info("本次验证码为:" + code);
                //调用qq发送验证码api
                QQMail.sendTestMail(phone,code);
                //将手机号和验证码分别存到session中保存
                session.setAttribute("phone", phone);
                session.setAttribute("code", code);
                return R.success("验证码已发送成功，请注意查收!");
            } else {
                throw new CustmException("手机号不能为空");
            }
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/login")
    public R<String> login(@RequestBody Map map, HttpSession session) {
        // 获取用户手机号
        String phone = map.get("phone").toString();
        // 获取验证码
        String code = map.get("code").toString();
        // 从session中获取保存的验证码
        String sessionCode = session.getAttribute("code").toString();
        if (sessionCode.equals(code) && phone != null) {
            // 比对成功,如果是新用户，就自动完成注册
            LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(User::getPhone,phone);
            User user = userService.getOne(wrapper);
            if (user==null){
                //TODO 将新用户保存
                user = new User();
                user.setId(IdWorker.getId());
                user.setPhone(phone);
                user.setName("用户"+sessionCode);
                userService.save(user);
                return R.success("验证成功，正在登录");
            }else {
                session.setAttribute("user",user.getId());
                return R.success("验证成功，正在登录");
            }
        }else {
            throw new CustmException("手机号不能为空，登录失败!");
        }

    }
}
