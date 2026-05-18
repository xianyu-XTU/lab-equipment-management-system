package com.xtu.labequipment.dto;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
@Data
public class ReturnDeviceRequest {
    @NotNull(message = "recordId required") private Long recordId;
    private String remark;
}
