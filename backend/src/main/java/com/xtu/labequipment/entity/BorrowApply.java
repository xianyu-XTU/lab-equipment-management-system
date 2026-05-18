package com.xtu.labequipment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("borrow_apply")
public class BorrowApply {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long deviceId;
    private Long userId;
    private String applyReason;
    private LocalDateTime expectedReturnTime;
    private LocalDateTime applyTime;
    private Integer status;
    private Long approveUserId;
    private LocalDateTime approveTime;
    private String approveRemark;
}
