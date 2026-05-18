package com.xtu.labequipment.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xtu.labequipment.common.AuthContext;
import com.xtu.labequipment.common.Result;
import com.xtu.labequipment.entity.BorrowApply;
import com.xtu.labequipment.entity.BorrowRecord;
import com.xtu.labequipment.entity.RepairRecord;
import com.xtu.labequipment.mapper.BorrowApplyMapper;
import com.xtu.labequipment.mapper.BorrowRecordMapper;
import com.xtu.labequipment.mapper.RepairRecordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/my")
@RequiredArgsConstructor
public class MyRecordController {
    private final BorrowApplyMapper borrowApplyMapper;
    private final BorrowRecordMapper borrowRecordMapper;
    private final RepairRecordMapper repairRecordMapper;

    @GetMapping("/applies")
    public Result<?> myApplies(@RequestParam(defaultValue = "1") long page,
                               @RequestParam(defaultValue = "10") long size,
                               @RequestParam(required = false) Integer status) {
        LambdaQueryWrapper<BorrowApply> w = new LambdaQueryWrapper<BorrowApply>()
                .eq(BorrowApply::getUserId, AuthContext.getUserId());
        if (status != null) w.eq(BorrowApply::getStatus, status);
        w.orderByDesc(BorrowApply::getApplyTime);
        return Result.ok(borrowApplyMapper.selectPage(new Page<>(page, size), w));
    }

    @GetMapping("/records")
    public Result<?> myRecords(@RequestParam(defaultValue = "1") long page,
                               @RequestParam(defaultValue = "10") long size,
                               @RequestParam(required = false) Integer status) {
        LambdaQueryWrapper<BorrowRecord> w = new LambdaQueryWrapper<BorrowRecord>()
                .eq(BorrowRecord::getUserId, AuthContext.getUserId());
        if (status != null) w.eq(BorrowRecord::getStatus, status);
        w.orderByDesc(BorrowRecord::getBorrowTime);
        return Result.ok(borrowRecordMapper.selectPage(new Page<>(page, size), w));
    }

    @GetMapping("/repairs")
    public Result<?> myRepairs(@RequestParam(defaultValue = "1") long page,
                               @RequestParam(defaultValue = "10") long size,
                               @RequestParam(required = false) Integer status) {
        LambdaQueryWrapper<RepairRecord> w = new LambdaQueryWrapper<RepairRecord>()
                .eq(RepairRecord::getUserId, AuthContext.getUserId());
        if (status != null) w.eq(RepairRecord::getStatus, status);
        w.orderByDesc(RepairRecord::getReportTime);
        return Result.ok(repairRecordMapper.selectPage(new Page<>(page, size), w));
    }
}
