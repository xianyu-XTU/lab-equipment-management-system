package com.xtu.labequipment.dto;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
@Data
public class ApproveBorrowRequest {
    @NotNull(message = "applyId required") private Long applyId;
    @NotNull(message = "status required") private Integer status;
    private String approveRemark;
}
