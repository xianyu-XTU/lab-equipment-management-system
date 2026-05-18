package com.xtu.labequipment.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xtu.labequipment.annotation.RequireRole;
import com.xtu.labequipment.common.AuthContext;
import com.xtu.labequipment.common.BusinessException;
import com.xtu.labequipment.common.Result;
import com.xtu.labequipment.dto.RepairHandleRequest;
import com.xtu.labequipment.entity.BorrowRecord;
import com.xtu.labequipment.entity.Device;
import com.xtu.labequipment.entity.RepairRecord;
import com.xtu.labequipment.entity.User;
import com.xtu.labequipment.mapper.BorrowRecordMapper;
import com.xtu.labequipment.mapper.DeviceMapper;
import com.xtu.labequipment.mapper.RepairRecordMapper;
import com.xtu.labequipment.mapper.UserMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/repairs")
@RequiredArgsConstructor
public class RepairController {
    private final RepairRecordMapper repairRecordMapper;
    private final DeviceMapper deviceMapper;
    private final BorrowRecordMapper borrowRecordMapper;
    private final UserMapper userMapper;

    @PostMapping("/report")
    public Result<?> report(@RequestBody RepairRecord repair) {
        if (repair == null || repair.getDeviceId() == null) throw new BusinessException("设备不能为空");
        if (repair.getFaultDesc() == null || repair.getFaultDesc().isBlank()) throw new BusinessException("故障描述不能为空");
        Device d = deviceMapper.selectById(repair.getDeviceId());
        if (d == null) throw new BusinessException("设备不存在");
        if (d.getStatus() != null && d.getStatus() == 4) throw new BusinessException("设备已停用，不能报修");

        if ("STUDENT".equals(AuthContext.getRoleCode())) {
            Long active = borrowRecordMapper.selectCount(new LambdaQueryWrapper<BorrowRecord>()
                    .eq(BorrowRecord::getDeviceId, repair.getDeviceId())
                    .eq(BorrowRecord::getUserId, AuthContext.getUserId())
                    .in(BorrowRecord::getStatus, 1, 3));
            if (active == null || active == 0) throw new BusinessException("学生只能报修自己正在借用的设备");
        }

        Long pending = repairRecordMapper.selectCount(new LambdaQueryWrapper<RepairRecord>()
                .eq(RepairRecord::getDeviceId, repair.getDeviceId())
                .in(RepairRecord::getStatus, 0, 1));
        if (pending != null && pending > 0) throw new BusinessException("该设备已有未完成报修记录");

        repair.setUserId(AuthContext.getUserId());
        repair.setReportTime(LocalDateTime.now());
        repair.setStatus(0);
        repairRecordMapper.insert(repair);
        d.setStatus(3);
        deviceMapper.updateById(d);
        return Result.ok();
    }

    @GetMapping
    public Result<?> page(@RequestParam(defaultValue = "1") long page,
                          @RequestParam(defaultValue = "10") long size,
                          @RequestParam(required = false) Integer status) {
        LambdaQueryWrapper<RepairRecord> w = new LambdaQueryWrapper<>();
        if (status != null) w.eq(RepairRecord::getStatus, status);
        if ("STUDENT".equals(AuthContext.getRoleCode())) w.eq(RepairRecord::getUserId, AuthContext.getUserId());
        w.orderByDesc(RepairRecord::getReportTime);
        Page<RepairRecord> result = repairRecordMapper.selectPage(new Page<>(page, size), w);
        Page<Map<String, Object>> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        voPage.setRecords(result.getRecords().stream().map(this::toMap).toList());
        return Result.ok(voPage);
    }

    @RequireRole({"LAB_ADMIN", "ADMIN"})
    @PostMapping("/handle")
    public Result<?> handle(@RequestBody @Valid RepairHandleRequest request) {
        RepairRecord r = repairRecordMapper.selectById(request.getRepairId());
        if (r == null) throw new BusinessException("repair not found");
        if (request.getStatus() == null || (request.getStatus() != 1 && request.getStatus() != 2)) throw new BusinessException("处理状态只能是1或2");
        r.setStatus(request.getStatus());
        r.setHandleUserId(AuthContext.getUserId());
        r.setHandleTime(LocalDateTime.now());
        r.setHandleResult(request.getHandleResult());
        repairRecordMapper.updateById(r);
        if (request.getStatus() == 2) {
            Device d = deviceMapper.selectById(r.getDeviceId());
            if (d != null) {
                Long activeBorrow = borrowRecordMapper.selectCount(new LambdaQueryWrapper<BorrowRecord>()
                        .eq(BorrowRecord::getDeviceId, r.getDeviceId())
                        .in(BorrowRecord::getStatus, 1, 3));
                d.setStatus(activeBorrow != null && activeBorrow > 0 ? 2 : 1);
                deviceMapper.updateById(d);
            }
        }
        return Result.ok();
    }

    private Map<String, Object> toMap(RepairRecord r) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", r.getId());
        m.put("deviceId", r.getDeviceId());
        m.put("userId", r.getUserId());
        m.put("faultDesc", r.getFaultDesc());
        m.put("reportTime", r.getReportTime());
        m.put("status", r.getStatus());
        m.put("handleUserId", r.getHandleUserId());
        m.put("handleTime", r.getHandleTime());
        m.put("handleResult", r.getHandleResult());
        Device d = deviceMapper.selectById(r.getDeviceId());
        User u = userMapper.selectById(r.getUserId());
        m.put("deviceName", d == null ? "" : d.getName());
        m.put("deviceNo", d == null ? "" : d.getDeviceNo());
        m.put("userName", u == null ? "" : u.getRealName());
        return m;
    }
}
