package com.xtu.labequipment.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.xtu.labequipment.common.AuthContext;
import com.xtu.labequipment.common.BusinessException;
import com.xtu.labequipment.dto.ApproveBorrowRequest;
import com.xtu.labequipment.dto.ReturnDeviceRequest;
import com.xtu.labequipment.entity.BorrowApply;
import com.xtu.labequipment.entity.BorrowRecord;
import com.xtu.labequipment.entity.Device;
import com.xtu.labequipment.mapper.BorrowApplyMapper;
import com.xtu.labequipment.mapper.BorrowRecordMapper;
import com.xtu.labequipment.mapper.DeviceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class BorrowBusinessService {
    private final BorrowApplyMapper borrowApplyMapper;
    private final BorrowRecordMapper borrowRecordMapper;
    private final DeviceMapper deviceMapper;

    @Transactional(rollbackFor = Exception.class)
    public void apply(BorrowApply apply) {
        Long userId = requireLoginUserId();
        if (apply == null || apply.getDeviceId() == null) throw new BusinessException("device required");
        if (apply.getExpectedReturnTime() == null) throw new BusinessException("expected return time required");
        if (!apply.getExpectedReturnTime().isAfter(LocalDateTime.now())) throw new BusinessException("expected return time must be future");
        Device device = deviceMapper.selectById(apply.getDeviceId());
        if (device == null) throw new BusinessException("device not found");
        if (!Objects.equals(device.getStatus(), 1)) throw new BusinessException("device unavailable");
        Long ownPending = borrowApplyMapper.selectCount(new LambdaQueryWrapper<BorrowApply>()
                .eq(BorrowApply::getUserId, userId).eq(BorrowApply::getDeviceId, apply.getDeviceId()).eq(BorrowApply::getStatus, 0));
        if (ownPending != null && ownPending > 0) throw new BusinessException("你已提交该设备的待审批申请，请勿重复提交");
        Long devicePending = borrowApplyMapper.selectCount(new LambdaQueryWrapper<BorrowApply>()
                .eq(BorrowApply::getDeviceId, apply.getDeviceId()).eq(BorrowApply::getStatus, 0));
        if (devicePending != null && devicePending > 0) throw new BusinessException("该设备已有待审批申请，请选择其他设备或稍后再试");
        Long activeRecord = borrowRecordMapper.selectCount(new LambdaQueryWrapper<BorrowRecord>()
                .eq(BorrowRecord::getDeviceId, apply.getDeviceId()).in(BorrowRecord::getStatus, 1, 3));
        if (activeRecord != null && activeRecord > 0) throw new BusinessException("该设备已有未归还记录");
        apply.setUserId(userId);
        apply.setApplyTime(LocalDateTime.now());
        apply.setStatus(0);
        borrowApplyMapper.insert(apply);
    }

    @Transactional(rollbackFor = Exception.class)
    public void approve(ApproveBorrowRequest request) {
        if (!Objects.equals(request.getStatus(), 1) && !Objects.equals(request.getStatus(), 2)) throw new BusinessException("status must be 1 or 2");
        BorrowApply apply = borrowApplyMapper.selectById(request.getApplyId());
        if (apply == null) throw new BusinessException("application not found");
        if (!Objects.equals(apply.getStatus(), 0)) throw new BusinessException("application already processed");
        apply.setStatus(request.getStatus());
        apply.setApproveUserId(requireLoginUserId());
        apply.setApproveTime(LocalDateTime.now());
        apply.setApproveRemark(request.getApproveRemark());
        borrowApplyMapper.updateById(apply);
        if (Objects.equals(request.getStatus(), 2)) return;

        Long active = borrowRecordMapper.selectCount(new LambdaQueryWrapper<BorrowRecord>()
                .eq(BorrowRecord::getDeviceId, apply.getDeviceId()).in(BorrowRecord::getStatus, 1, 3));
        if (active != null && active > 0) throw new BusinessException("device has active borrow record");

        int updated = deviceMapper.update(null, new LambdaUpdateWrapper<Device>()
                .eq(Device::getId, apply.getDeviceId()).eq(Device::getStatus, 1).set(Device::getStatus, 2));
        if (updated == 0) throw new BusinessException("device status changed, cannot approve");

        BorrowRecord record = new BorrowRecord();
        record.setApplyId(apply.getId());
        record.setDeviceId(apply.getDeviceId());
        record.setUserId(apply.getUserId());
        record.setBorrowTime(LocalDateTime.now());
        record.setStatus(1);
        record.setIsOverdue(0);
        borrowRecordMapper.insert(record);
    }

    @Transactional(rollbackFor = Exception.class)
    public void returnDevice(ReturnDeviceRequest request) {
        BorrowRecord record = borrowRecordMapper.selectById(request.getRecordId());
        if (record == null) throw new BusinessException("borrow record not found");
        if (!Objects.equals(record.getStatus(), 1) && !Objects.equals(record.getStatus(), 3)) throw new BusinessException("record is not borrowing");
        LocalDateTime now = LocalDateTime.now();
        BorrowApply apply = borrowApplyMapper.selectById(record.getApplyId());
        boolean overdue = apply != null && apply.getExpectedReturnTime() != null && now.isAfter(apply.getExpectedReturnTime());
        record.setReturnTime(now);
        record.setRemark(request.getRemark());
        record.setStatus(2);
        record.setIsOverdue(overdue ? 1 : 0);
        borrowRecordMapper.updateById(record);
        deviceMapper.update(null, new LambdaUpdateWrapper<Device>().eq(Device::getId, record.getDeviceId()).set(Device::getStatus, 1));
    }

    private Long requireLoginUserId() {
        Long id = AuthContext.getUserId();
        if (id == null) throw new BusinessException("please login");
        return id;
    }
}
