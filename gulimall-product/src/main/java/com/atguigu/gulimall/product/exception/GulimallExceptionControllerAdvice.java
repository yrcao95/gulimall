package com.atguigu.gulimall.product.exception;


import com.atguigu.common.exception.BizCodeEnum;
import com.atguigu.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@ResponseBody
@ControllerAdvice(basePackages = "com.atguigu.gulimall.product.controller")
public class GulimallExceptionControllerAdvice {

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public R handleValidException(MethodArgumentNotValidException e) {
        log.error("Validation failed, {}, Exception Class: ", e.getMessage(), e.getClass());
        BindingResult result=e.getBindingResult();
        Map<String, String> map=new HashMap<>();
        result.getFieldErrors().forEach((item)->{
            String field=item.getField();
            String message=item.getDefaultMessage();
            map.put(field, message);
        });
        return R.error(BizCodeEnum.VALID_EXCEPTION.getCode(), BizCodeEnum.VALID_EXCEPTION.getMessage()).put("data", map);
    }

    @ExceptionHandler(value=Throwable.class)
    public R handleException(Throwable throwable) {
        return R.error(BizCodeEnum.UNKNOWN_EXCEPTION.getCode(), BizCodeEnum.UNKNOWN_EXCEPTION.getMessage());
    }
}
