package com.xtu.labequipment.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xtu.labequipment.annotation.RequireRole;
import com.xtu.labequipment.common.Result;
import com.xtu.labequipment.entity.BorrowApply;
import com.xtu.labequipment.entity.BorrowRecord;
import com.xtu.labequipment.entity.Device;
import com.xtu.labequipment.entity.RepairRecord;
import com.xtu.labequipment.mapper.BorrowApplyMapper;
import com.xtu.labequipment.mapper.BorrowRecordMapper;
import com.xtu.labequipment.mapper.DeviceMapper;
import com.xtu.labequipment.mapper.RepairRecordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatsController {
    private final DeviceMapper deviceMapper;
    private final BorrowApplyMapper borrowApplyMapper;
    private final BorrowRecordMapper borrowRecordMapper;
    private final RepairRecordMapper repairRecordMapper;

    @RequireRole({"ADMIN", "LAB_ADMIN"})
    @GetMapping("/overview")
    public Result<?> overview() {
        Map<String, Object> m = new HashMap<>();
        m.put("deviceTotal", deviceMapper.selectCount(null));
        m.put("availableDevices", deviceMapper.selectCount(new LambdaQueryWrapper<Device>().eq(Device::getStatus, 1)));
        m.put("borrowedDevices", deviceMapper.selectCount(new LambdaQueryWrapper<Device>().eq(Device::getStatus, 2)));
        m.put("repairingDevices", deviceMapper.selectCount(new LambdaQueryWrapper<Device>().eq(Device::getStatus, 3)));
        m.put("pendingApplies", borrowApplyMapper.selectCount(new LambdaQueryWrapper<BorrowApply>().eq(BorrowApply::getStatus, 0)));
        m.put("borrowingRecords", borrowRecordMapper.selectCount(new LambdaQueryWrapper<BorrowRecord>().in(BorrowRecord::getStatus, 1, 3)));
        m.put("pendingRepairs", repairRecordMapper.selectCount(new LambdaQueryWrapper<RepairRecord>().eq(RepairRecord::getStatus, 0)));
        return Result.ok(m);
    }
}
