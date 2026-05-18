package com.xtu.labequipment.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {
    private Integer code;
    private String message;
    private T data;

    public static <T> Result<T> ok() { return new Result<>(200, "success", null); }
    public static <T> Result<T> ok(T data) { return new Result<>(200, "success", data); }
    public static <T> Result<T> fail(String message) { return new Result<>(500, message, null); }
    public static <T> Result<T> unauthorized(String message) { return new Result<>(401, message, null); }
    public static <T> Result<T> forbidden(String message) { return new Result<>(403, message, null); }
}
