package com.xtu.labequipment.common;

import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusiness(BusinessException e) { return Result.fail(e.getMessage()); }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<?> handleValid(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().isEmpty() ? "parameter error" : e.getBindingResult().getFieldErrors().get(0).getDefaultMessage();
        return Result.fail(msg);
    }

    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) { return Result.fail(e.getMessage()); }
}
