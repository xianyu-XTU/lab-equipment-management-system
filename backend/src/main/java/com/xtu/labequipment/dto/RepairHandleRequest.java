package com.xtu.labequipment.dto;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
@Data
public class RepairHandleRequest {
    @NotNull(message = "repairId required") private Long repairId;
    @NotNull(message = "status required") private Integer status;
    private String handleResult;
}
