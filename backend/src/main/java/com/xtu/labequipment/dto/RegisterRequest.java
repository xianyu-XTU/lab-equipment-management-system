package com.xtu.labequipment.dto;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
@Data
public class RegisterRequest {
    @NotBlank(message = "username required") private String username;
    @NotBlank(message = "password required") private String password;
    private String realName;
    private String phone;
    private String email;
}
