package com.ERP_SYSTEM.common.exception;

import com.ERP_SYSTEM.common.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.nio.file.AccessDeniedException;
import java.util.HashMap;
import java.util.Map;


@Slf4j // để ghi log
@RestControllerAdvice
public class GlobalExceptionHandler {

    //Bắt lỗi thrown
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Object>> handlerRuntimeException(RuntimeException ex) {
        ApiResponse<Object> errors = ApiResponse.errors("Có lỗi xảy ra",ex.getMessage());
        return ResponseEntity.badRequest().body(errors);
    }

    //Bắt lỗi valid fail
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handlerMethodArgumentNotValidException(MethodArgumentNotValidException ex){
        Map<String , String> fieldErrors  = new HashMap<>();// gom tất cả lỗi vào rhanhf 1 mảng

        ex.getBindingResult().getAllErrors().forEach(
                error -> {
                    String field = ((FieldError) error).getField();

                    String message = error.getDefaultMessage();

                    fieldErrors.put(field, message);
                }
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.errors("Dữ liệu không hợp lệ", fieldErrors.toString()));
    }
    //bắt lỗi username và password
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Object>> HandlerBadCredentialsException(BadCredentialsException ex){
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.errors(
                        "Đăng nhập thất bại",
                        "Username hoặc password không đúng"));
    }

    //bắt lỗi tài khoản vô hiệu hoá
    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ApiResponse<Object>> handlerDisableException(DisabledException ex){
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.errors(
                        "Đăng nhập thất bại",
                        "Tài khoản bị vô hiệu hoá"
                ));
    }
    //bắt lỗi role
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Object>> handleAccessDenied(
            AccessDeniedException ex) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.errors(
                        "Không có quyền",
                        "Bạn không có quyền thực hiện thao tác này"
                ));
    }

    //lỗi hệ thóng
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGeneral(
            Exception ex) {
        log.error("Lỗi hệ thống: ", ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.errors(
                        "Lỗi hệ thống",
                        "Đã xảy ra lỗi, vui lòng thử lại sau"
                ));
    }


}
