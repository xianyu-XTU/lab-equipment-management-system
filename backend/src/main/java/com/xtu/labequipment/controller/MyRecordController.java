package com.xtu.labequipment.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xtu.labequipment.common.AuthContext;
import com.xtu.labequipment.common.BusinessException;
import com.xtu.labequipment.common.Result;
import com.xtu.labequipment.entity.BorrowApply;
import com.xtu.labequipment.entity.BorrowRecord;
import com.xtu.labequipment.entity.RepairRecord;
import com.xtu.labequipment.service.BorrowApplyService;
import com.xtu.labequipment.service.BorrowRecordService;
import com.xtu.labequipment.service.RepairRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/my")
@RequiredArgsConstructor
public class MyRecordController {

    private final BorrowApplyService borrowApplyService;
    private final BorrowRecordService borrowRecordService;
    private final RepairRecordService repairRecordService;

    @GetMapping("/borrow-applies")
    public Result<Page<BorrowApply>> myBorrowApplies(@RequestParam(defaultValue = "1") long page,
                                                     @RequestParam(defaultValue = "10") long size,
                                                     @RequestParam(required = false) Integer status) {
        Long userId = requireLoginUserId();
        LambdaQueryWrapper<BorrowApply> wrapper = new LambdaQueryWrapper<BorrowApply>()
                .eq(BorrowApply::getUserId, userId)
                .orderByDesc(BorrowApply::getApplyTime);
        if (status != null) {
            wrapper.eq(BorrowApply::getStatus, status);
        }
        return Result.ok(borrowApplyService.page(new Page<>(page, size), wrapper));
    }

    @GetMapping("/borrow-records")
    public Result<Page<BorrowRecord>> myBorrowRecords(@RequestParam(defaultValue = "1") long page,
                                                       @RequestParam(defaultValue = "10") long size,
                                                       @RequestParam(required = false) Integer status) {
        Long userId = requireLoginUserId();
        LambdaQueryWrapper<BorrowRecord> wrapper = new LambdaQueryWrapper<BorrowRecord>()
                .eq(BorrowRecord::getUserId, userId)
                .orderByDesc(BorrowRecord::getBorrowTime);
        if (status != null) {
            wrapper.eq(BorrowRecord::getStatus, status);
        }
        return Result.ok(borrowRecordService.page(new Page<>(page, size), wrapper));
    }

    @GetMapping("/repairs")
    public Result<Page<RepairRecord>> myRepairs(@RequestParam(defaultValue = "1") long page,
                                                @RequestParam(defaultValue = "10") long size,
                                                @RequestParam(required = false) Integer repairStatus) {
        Long userId = requireLoginUserId();
        LambdaQueryWrapper<RepairRecord> wrapper = new LambdaQueryWrapper<RepairRecord>()
                .eq(RepairRecord::getUserId, userId)
                .orderByDesc(RepairRecord::getReportTime);
        if (repairStatus != null) {
            wrapper.eq(RepairRecord::getRepairStatus, repairStatus);
        }
        return Result.ok(repairRecordService.page(new Page<>(page, size), wrapper));
    }

    private Long requireLoginUserId() {
        Long userId = AuthContext.getUserId();
        if (userId == null) {
            throw new BusinessException("用户未登录");
        }
        return userId;
    }
}
