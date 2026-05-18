package com.xtu.labequipment.common;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TokenUser {
    private Long userId;
    private String username;
    private String roleCode;
}
