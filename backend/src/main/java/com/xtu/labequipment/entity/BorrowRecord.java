package com.xtu.labequipment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("borrow_record")
public class BorrowRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long applyId;
    private Long deviceId;
    private Long userId;
    private LocalDateTime borrowTime;
    private LocalDateTime returnTime;
    private Integer status;
    private Integer isOverdue;
    private String remark;
}
