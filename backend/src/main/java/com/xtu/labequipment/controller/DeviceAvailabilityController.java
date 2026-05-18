package com.xtu.labequipment.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xtu.labequipment.common.Result;
import com.xtu.labequipment.entity.BorrowApply;
import com.xtu.labequipment.entity.BorrowRecord;
import com.xtu.labequipment.entity.Device;
import com.xtu.labequipment.mapper.BorrowApplyMapper;
import com.xtu.labequipment.mapper.BorrowRecordMapper;
import com.xtu.labequipment.mapper.DeviceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/devices")
@RequiredArgsConstructor
public class DeviceAvailabilityController {
    private final DeviceMapper deviceMapper;
    private final BorrowApplyMapper borrowApplyMapper;
    private final BorrowRecordMapper borrowRecordMapper;

    @GetMapping("/{id}/availability")
    public Result<?> availability(@PathVariable Long id) {
        Map<String, Object> data = new HashMap<>();
        Device device = deviceMapper.selectById(id);
        if (device == null) {
            data.put("available", false);
            data.put("reason", "设备不存在");
            return Result.ok(data);
        }
        if (device.getStatus() == null || device.getStatus() != 1) {
            data.put("available", false);
            data.put("reason", "设备当前状态不可借");
            data.put("deviceStatus", device.getStatus());
            return Result.ok(data);
        }
        Long activeRecords = borrowRecordMapper.selectCount(new LambdaQueryWrapper<BorrowRecord>()
                .eq(BorrowRecord::getDeviceId, id).in(BorrowRecord::getStatus, 1, 3));
        if (activeRecords != null && activeRecords > 0) {
            data.put("available", false);
            data.put("reason", "设备存在未归还记录");
            return Result.ok(data);
        }
        Long pendingApplies = borrowApplyMapper.selectCount(new LambdaQueryWrapper<BorrowApply>()
                .eq(BorrowApply::getDeviceId, id).eq(BorrowApply::getStatus, 0));
        if (pendingApplies != null && pendingApplies > 0) {
            data.put("available", false);
            data.put("reason", "设备已有待审批申请");
            data.put("pendingApplyCount", pendingApplies);
            return Result.ok(data);
        }
        data.put("available", true);
        data.put("reason", "设备可借");
        data.put("pendingApplyCount", 0);
        return Result.ok(data);
    }
}
