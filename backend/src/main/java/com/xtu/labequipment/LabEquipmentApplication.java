package com.xtu.labequipment;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.xtu.labequipment.mapper")
public class LabEquipmentApplication {
    public static void main(String[] args) {
        SpringApplication.run(LabEquipmentApplication.class, args);
    }
}
