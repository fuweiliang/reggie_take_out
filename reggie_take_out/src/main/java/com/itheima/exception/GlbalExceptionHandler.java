package com.itheima.exception;

import com.itheima.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLIntegrityConstraintViolationException;

@Slf4j
@ResponseBody
@ControllerAdvice(annotations = {RestController.class, Controller.class})
public class GlbalExceptionHandler {
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public R<String> exceptionHandler(SQLIntegrityConstraintViolationException e) {
        //将捕获到的异常截断反馈给前端
        if (e.getMessage().contains("Duplicate entry")) {
            String[] apilt = e.getMessage().split(" ");
            String msg = apilt[2] + "已存在";
            return R.error(msg);
        }
        log.error(e.getMessage());
        return R.error("操作失败");
    }

    @ExceptionHandler(CustmException.class)
    public R<String> custmexception(CustmException e) {
        //将业务一场返回给前端
        return R.error(e.getMessage());
    }
}
