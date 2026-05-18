package com.xtu.labequipment.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xtu.labequipment.annotation.RequireRole;
import com.xtu.labequipment.common.AuthContext;
import com.xtu.labequipment.common.Result;
import com.xtu.labequipment.dto.ApproveBorrowRequest;
import com.xtu.labequipment.dto.ReturnDeviceRequest;
import com.xtu.labequipment.entity.BorrowApply;
import com.xtu.labequipment.entity.BorrowRecord;
import com.xtu.labequipment.entity.Device;
import com.xtu.labequipment.entity.User;
import com.xtu.labequipment.mapper.BorrowApplyMapper;
import com.xtu.labequipment.mapper.BorrowRecordMapper;
import com.xtu.labequipment.mapper.DeviceMapper;
import com.xtu.labequipment.mapper.UserMapper;
import com.xtu.labequipment.service.BorrowBusinessService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/borrow")
@RequiredArgsConstructor
public class BorrowController {
    private final BorrowApplyMapper borrowApplyMapper;
    private final BorrowRecordMapper borrowRecordMapper;
    private final DeviceMapper deviceMapper;
    private final UserMapper userMapper;
    private final BorrowBusinessService borrowBusinessService;

    @PostMapping("/apply")
    public Result<?> apply(@RequestBody BorrowApply apply) { borrowBusinessService.apply(apply); return Result.ok(); }

    @GetMapping("/applies")
    public Result<?> applies(@RequestParam(defaultValue = "1") long page,
                             @RequestParam(defaultValue = "10") long size,
                             @RequestParam(required = false) Integer status,
                             @RequestParam(required = false) Long userId) {
        LambdaQueryWrapper<BorrowApply> w = new LambdaQueryWrapper<>();
        if (status != null) w.eq(BorrowApply::getStatus, status);
        if ("STUDENT".equals(AuthContext.getRoleCode())) w.eq(BorrowApply::getUserId, AuthContext.getUserId());
        else if (userId != null) w.eq(BorrowApply::getUserId, userId);
        w.orderByDesc(BorrowApply::getApplyTime);
        Page<BorrowApply> result = borrowApplyMapper.selectPage(new Page<>(page, size), w);
        Page<Map<String, Object>> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        voPage.setRecords(result.getRecords().stream().map(this::applyToMap).toList());
        return Result.ok(voPage);
    }

    @RequireRole({"LAB_ADMIN", "ADMIN"})
    @PostMapping("/approve")
    public Result<?> approve(@RequestBody @Valid ApproveBorrowRequest request) { borrowBusinessService.approve(request); return Result.ok(); }

    @GetMapping("/records")
    public Result<?> records(@RequestParam(defaultValue = "1") long page,
                             @RequestParam(defaultValue = "10") long size,
                             @RequestParam(required = false) Integer status,
                             @RequestParam(required = false) Long userId) {
        LambdaQueryWrapper<BorrowRecord> w = new LambdaQueryWrapper<>();
        if (status != null) w.eq(BorrowRecord::getStatus, status);
        if ("STUDENT".equals(AuthContext.getRoleCode())) w.eq(BorrowRecord::getUserId, AuthContext.getUserId());
        else if (userId != null) w.eq(BorrowRecord::getUserId, userId);
        w.orderByDesc(BorrowRecord::getBorrowTime);
        Page<BorrowRecord> result = borrowRecordMapper.selectPage(new Page<>(page, size), w);
        Page<Map<String, Object>> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        voPage.setRecords(result.getRecords().stream().map(this::recordToMap).toList());
        return Result.ok(voPage);
    }

    @RequireRole({"LAB_ADMIN", "ADMIN"})
    @PostMapping("/return")
    public Result<?> returnDevice(@RequestBody @Valid ReturnDeviceRequest request) { borrowBusinessService.returnDevice(request); return Result.ok(); }

    private Map<String, Object> applyToMap(BorrowApply a) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", a.getId());
        m.put("deviceId", a.getDeviceId());
        m.put("userId", a.getUserId());
        m.put("applyReason", a.getApplyReason());
        m.put("expectedReturnTime", a.getExpectedReturnTime());
        m.put("applyTime", a.getApplyTime());
        m.put("status", a.getStatus());
        m.put("approveUserId", a.getApproveUserId());
        m.put("approveTime", a.getApproveTime());
        m.put("approveRemark", a.getApproveRemark());
        Device d = deviceMapper.selectById(a.getDeviceId());
        User u = userMapper.selectById(a.getUserId());
        m.put("deviceName", d == null ? "" : d.getName());
        m.put("deviceNo", d == null ? "" : d.getDeviceNo());
        m.put("userName", u == null ? "" : u.getRealName());
        return m;
    }

    private Map<String, Object> recordToMap(BorrowRecord r) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", r.getId());
        m.put("applyId", r.getApplyId());
        m.put("deviceId", r.getDeviceId());
        m.put("userId", r.getUserId());
        m.put("borrowTime", r.getBorrowTime());
        m.put("returnTime", r.getReturnTime());
        m.put("status", r.getStatus());
        m.put("isOverdue", r.getIsOverdue());
        m.put("remark", r.getRemark());
        Device d = deviceMapper.selectById(r.getDeviceId());
        User u = userMapper.selectById(r.getUserId());
        m.put("deviceName", d == null ? "" : d.getName());
        m.put("deviceNo", d == null ? "" : d.getDeviceNo());
        m.put("userName", u == null ? "" : u.getRealName());
        return m;
    }
}
