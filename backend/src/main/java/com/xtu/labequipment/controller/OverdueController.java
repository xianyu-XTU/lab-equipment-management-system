package com.xtu.labequipment.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xtu.labequipment.annotation.RequireRole;
import com.xtu.labequipment.common.Result;
import com.xtu.labequipment.dto.OverdueRecordVO;
import com.xtu.labequipment.entity.BorrowApply;
import com.xtu.labequipment.entity.BorrowRecord;
import com.xtu.labequipment.mapper.BorrowApplyMapper;
import com.xtu.labequipment.mapper.BorrowRecordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/overdue")
@RequiredArgsConstructor
@RequireRole({"LAB_ADMIN", "ADMIN"})
public class OverdueController {

    private final BorrowRecordMapper borrowRecordMapper;
    private final BorrowApplyMapper borrowApplyMapper;

    @GetMapping("/records")
    public Result<List<OverdueRecordVO>> overdueRecords() {
        LocalDateTime now = LocalDateTime.now();
        List<BorrowRecord> records = borrowRecordMapper.selectList(new LambdaQueryWrapper<BorrowRecord>()
                .in(BorrowRecord::getStatus, 1, 3)
                .orderByDesc(BorrowRecord::getBorrowTime));

        List<OverdueRecordVO> result = new ArrayList<>();
        for (BorrowRecord record : records) {
            BorrowApply apply = borrowApplyMapper.selectById(record.getApplyId());
            if (apply == null || apply.getExpectedReturnTime() == null || !apply.getExpectedReturnTime().isBefore(now)) {
                continue;
            }
            OverdueRecordVO vo = new OverdueRecordVO();
            vo.setRecordId(record.getId());
            vo.setApplyId(record.getApplyId());
            vo.setDeviceId(record.getDeviceId());
            vo.setUserId(record.getUserId());
            vo.setBorrowTime(record.getBorrowTime());
            vo.setExpectedReturnTime(apply.getExpectedReturnTime());
            vo.setOverdueHours(ChronoUnit.HOURS.between(apply.getExpectedReturnTime(), now));
            vo.setStatus(record.getStatus());
            result.add(vo);
        }
        return Result.ok(result);
    }

    @PostMapping("/refresh")
    public Result<Map<String, Object>> refreshOverdueStatus() {
        LocalDateTime now = LocalDateTime.now();
        List<BorrowRecord> records = borrowRecordMapper.selectList(new LambdaQueryWrapper<BorrowRecord>()
                .eq(BorrowRecord::getStatus, 1));

        int updated = 0;
        for (BorrowRecord record : records) {
            BorrowApply apply = borrowApplyMapper.selectById(record.getApplyId());
            if (apply == null || apply.getExpectedReturnTime() == null || !apply.getExpectedReturnTime().isBefore(now)) {
                continue;
            }
            record.setStatus(3);
            borrowRecordMapper.updateById(record);
            updated++;
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("updated", updated);
        data.put("checkedAt", now);
        return Result.ok(data);
    }
}
