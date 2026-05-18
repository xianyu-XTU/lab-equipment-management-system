package com.xtu.labequipment.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xtu.labequipment.annotation.RequireRole;
import com.xtu.labequipment.common.Result;
import com.xtu.labequipment.entity.BorrowApply;
import com.xtu.labequipment.entity.BorrowRecord;
import com.xtu.labequipment.entity.Device;
import com.xtu.labequipment.entity.User;
import com.xtu.labequipment.mapper.BorrowApplyMapper;
import com.xtu.labequipment.mapper.BorrowRecordMapper;
import com.xtu.labequipment.mapper.DeviceMapper;
import com.xtu.labequipment.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/overdue")
@RequiredArgsConstructor
@RequireRole({"LAB_ADMIN", "ADMIN"})
public class OverdueController {
    private final BorrowRecordMapper borrowRecordMapper;
    private final BorrowApplyMapper borrowApplyMapper;
    private final DeviceMapper deviceMapper;
    private final UserMapper userMapper;

    @PostMapping("/refresh")
    public Result<?> refresh() {
        List<BorrowRecord> records = borrowRecordMapper.selectList(new LambdaQueryWrapper<BorrowRecord>().eq(BorrowRecord::getStatus, 1));
        LocalDateTime now = LocalDateTime.now();
        int count = 0;
        for (BorrowRecord record : records) {
            BorrowApply apply = borrowApplyMapper.selectById(record.getApplyId());
            if (apply != null && apply.getExpectedReturnTime() != null && now.isAfter(apply.getExpectedReturnTime())) {
                record.setStatus(3);
                record.setIsOverdue(1);
                borrowRecordMapper.updateById(record);
                count++;
            }
        }
        Map<String, Object> data = new HashMap<>();
        data.put("updated", count);
        return Result.ok(data);
    }

    @GetMapping("/records")
    public Result<?> records(@RequestParam(defaultValue = "1") long page,
                             @RequestParam(defaultValue = "10") long size) {
        LambdaQueryWrapper<BorrowRecord> w = new LambdaQueryWrapper<BorrowRecord>()
                .eq(BorrowRecord::getStatus, 3)
                .orderByDesc(BorrowRecord::getBorrowTime);
        Page<BorrowRecord> result = borrowRecordMapper.selectPage(new Page<>(page, size), w);
        Page<Map<String, Object>> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        voPage.setRecords(result.getRecords().stream().map(this::toMap).toList());
        return Result.ok(voPage);
    }

    private Map<String, Object> toMap(BorrowRecord r) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", r.getId());
        m.put("deviceId", r.getDeviceId());
        m.put("userId", r.getUserId());
        m.put("borrowTime", r.getBorrowTime());
        m.put("returnTime", r.getReturnTime());
        m.put("status", r.getStatus());
        m.put("isOverdue", r.getIsOverdue());
        Device d = deviceMapper.selectById(r.getDeviceId());
        User u = userMapper.selectById(r.getUserId());
        m.put("deviceName", d == null ? "" : d.getName());
        m.put("deviceNo", d == null ? "" : d.getDeviceNo());
        m.put("userName", u == null ? "" : u.getRealName());
        return m;
    }
}
