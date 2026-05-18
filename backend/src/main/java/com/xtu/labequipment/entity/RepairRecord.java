package com.xtu.labequipment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("repair_record")
public class RepairRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long deviceId;
    private Long userId;
    private String faultDesc;
    private LocalDateTime reportTime;
    private Integer status;
    private Long handleUserId;
    private LocalDateTime handleTime;
    private String handleResult;
}
