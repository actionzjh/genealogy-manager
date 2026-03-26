package com.genealogy.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * 全局异常处理 - 统一返回友好错误信息，不暴露栈信息
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 参数校验失败（@Valid 注解触发的校验错误）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, Object> result = new HashMap<>();
        String errorMsg = ex.getBindingResult().getFieldError() != null
                ? ex.getBindingResult().getFieldError().getDefaultMessage()
                : "参数校验失败";
        result.put("code", 400);
        result.put("success", false);
        result.put("message", errorMsg);
        return ResponseEntity.badRequest().body(result);
    }

    /**
     * 参数绑定失败
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<Map<String, Object>> handleBindException(BindException ex) {
        Map<String, Object> result = new HashMap<>();
        String errorMsg = ex.getBindingResult().getFieldError() != null
                ? ex.getBindingResult().getFieldError().getDefaultMessage()
                : "参数绑定失败";
        result.put("code", 400);
        result.put("success", false);
        result.put("message", errorMsg);
        return ResponseEntity.badRequest().body(result);
    }

    /**
     * 请求体解析失败（JSON格式错误、缺少必要字段等）
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        Map<String, Object> result = new HashMap<>();
        String msg = ex.getMessage();
        // 提供友好提示而非原始异常信息
        result.put("code", 400);
        result.put("success", false);
        result.put("message", "请求格式错误，请检查输入内容");
        log.warn("请求体解析失败: {}", msg);
        return ResponseEntity.badRequest().body(result);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(AccessDeniedException ex) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 403);
        result.put("success", false);
        result.put("message", "无权访问此资源");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(result);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 400);
        result.put("success", false);
        result.put("message", ex.getMessage());
        return ResponseEntity.badRequest().body(result);
    }

    /**
     * 所有未处理的异常，返回通用错误信息
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleException(Exception ex) {
        log.error("系统异常: ", ex);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 500);
        result.put("success", false);
        result.put("message", "服务器内部错误，请稍后重试");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
    }
}
