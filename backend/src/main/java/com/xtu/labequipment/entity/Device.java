package com.xtu.labequipment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("device")
public class Device {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long categoryId;
    private String deviceNo;
    private String name;
    private String model;
    private String location;
    private Integer status;
    private LocalDate purchaseDate;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
