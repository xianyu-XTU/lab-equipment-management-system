package com.xtu.labequipment.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xtu.labequipment.annotation.RequireRole;
import com.xtu.labequipment.common.AuthContext;
import com.xtu.labequipment.common.Result;
import com.xtu.labequipment.entity.Notice;
import com.xtu.labequipment.mapper.NoticeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/notices")
@RequiredArgsConstructor
public class NoticeController {
    private final NoticeMapper noticeMapper;

    @GetMapping
    public Result<?> page(@RequestParam(defaultValue = "1") long page, @RequestParam(defaultValue = "10") long size) {
        LambdaQueryWrapper<Notice> w = new LambdaQueryWrapper<Notice>().eq(Notice::getStatus, 1).orderByDesc(Notice::getPublishTime);
        return Result.ok(noticeMapper.selectPage(new Page<>(page, size), w));
    }

    @RequireRole({"ADMIN"})
    @PostMapping
    public Result<?> save(@RequestBody Notice notice) {
        notice.setPublishUserId(AuthContext.getUserId());
        notice.setPublishTime(LocalDateTime.now());
        if (notice.getStatus() == null) notice.setStatus(1);
        noticeMapper.insert(notice);
        return Result.ok();
    }
}
