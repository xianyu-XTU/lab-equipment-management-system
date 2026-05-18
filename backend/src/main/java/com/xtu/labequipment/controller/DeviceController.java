package com.xtu.labequipment.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xtu.labequipment.annotation.RequireRole;
import com.xtu.labequipment.common.Result;
import com.xtu.labequipment.entity.Device;
import com.xtu.labequipment.entity.DeviceCategory;
import com.xtu.labequipment.mapper.DeviceCategoryMapper;
import com.xtu.labequipment.mapper.DeviceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class DeviceController {
    private final DeviceMapper deviceMapper;
    private final DeviceCategoryMapper categoryMapper;

    @GetMapping("/api/devices")
    public Result<?> devices(@RequestParam(defaultValue = "1") long page,
                             @RequestParam(defaultValue = "10") long size,
                             @RequestParam(required = false) String keyword,
                             @RequestParam(required = false) Integer status) {
        LambdaQueryWrapper<Device> w = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) w.and(q -> q.like(Device::getName, keyword).or().like(Device::getDeviceNo, keyword));
        if (status != null) w.eq(Device::getStatus, status);
        w.orderByDesc(Device::getId);
        return Result.ok(deviceMapper.selectPage(new Page<>(page, size), w));
    }

    @RequireRole({"ADMIN", "LAB_ADMIN"})
    @PostMapping("/api/devices")
    public Result<?> save(@RequestBody Device device) { if (device.getStatus() == null) device.setStatus(1); deviceMapper.insert(device); return Result.ok(); }

    @RequireRole({"ADMIN", "LAB_ADMIN"})
    @PutMapping("/api/devices")
    public Result<?> update(@RequestBody Device device) { deviceMapper.updateById(device); return Result.ok(); }

    @RequireRole({"ADMIN"})
    @DeleteMapping("/api/devices/{id}")
    public Result<?> delete(@PathVariable Long id) { deviceMapper.deleteById(id); return Result.ok(); }

    @GetMapping("/api/categories")
    public Result<?> categories() { return Result.ok(categoryMapper.selectList(new LambdaQueryWrapper<DeviceCategory>().orderByAsc(DeviceCategory::getId))); }

    @RequireRole({"ADMIN", "LAB_ADMIN"})
    @PostMapping("/api/categories")
    public Result<?> saveCategory(@RequestBody DeviceCategory category) { categoryMapper.insert(category); return Result.ok(); }
}
