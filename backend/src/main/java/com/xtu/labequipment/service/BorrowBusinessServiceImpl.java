package com.xtu.labequipment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
import com.xtu.labequipment.service.BorrowBusinessService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class BorrowBusinessServiceImpl implements BorrowBusinessService {

    private final BorrowApplyMapper borrowApplyMapper;
    private final BorrowRecordMapper borrowRecordMapper;
    private final DeviceMapper deviceMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void apply(BorrowApply apply) {
        Long userId = requireLoginUserId();
        if (apply == null || apply.getDeviceId() == null) {
            throw new BusinessException("借用设备不能为空");
        }
        if (apply.getExpectedReturnTime() == null) {
            throw new BusinessException("预计归还时间不能为空");
        }
        if (!apply.getExpectedReturnTime().isAfter(LocalDateTime.now())) {
            throw new BusinessException("预计归还时间必须晚于当前时间");
        }

        Device device = deviceMapper.selectById(apply.getDeviceId());
        if (device == null) {
            throw new BusinessException("设备不存在");
        }
        if (!Objects.equals(device.getStatus(), 1)) {
            throw new BusinessException("设备当前不可借");
        }

        Long pendingApplyCount = borrowApplyMapper.selectCount(new LambdaQueryWrapper<BorrowApply>()
                .eq(BorrowApply::getUserId, userId)
                .eq(BorrowApply::getDeviceId, apply.getDeviceId())
                .eq(BorrowApply::getStatus, 0));
        if (pendingApplyCount != null && pendingApplyCount > 0) {
            throw new BusinessException("该设备已有待审批申请，请勿重复提交");
        }

        Long activeRecordCount = borrowRecordMapper.selectCount(new LambdaQueryWrapper<BorrowRecord>()
                .eq(BorrowRecord::getDeviceId, apply.getDeviceId())
                .in(BorrowRecord::getStatus, 1, 3));
        if (activeRecordCount != null && activeRecordCount > 0) {
            throw new BusinessException("该设备已有未归还记录");
        }

        apply.setUserId(userId);
        apply.setApplyTime(LocalDateTime.now());
        apply.setStatus(0);
        borrowApplyMapper.insert(apply);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approve(ApproveBorrowRequest request) {
        if (request == null || request.getApplyId() == null) {
            throw new BusinessException("申请记录不能为空");
        }
        if (!Objects.equals(request.getStatus(), 1) && !Objects.equals(request.getStatus(), 2)) {
            throw new BusinessException("审批状态只能为通过或拒绝");
        }

        BorrowApply apply = borrowApplyMapper.selectById(request.getApplyId());
        if (apply == null) {
            throw new BusinessException("申请不存在");
        }
        if (!Objects.equals(apply.getStatus(), 0)) {
            throw new BusinessException("该申请已审批，不能重复处理");
        }

        apply.setStatus(request.getStatus());
        apply.setApproveUserId(requireLoginUserId());
        apply.setApproveTime(LocalDateTime.now());
        apply.setApproveRemark(request.getApproveRemark());
        borrowApplyMapper.updateById(apply);

        if (Objects.equals(request.getStatus(), 2)) {
            return;
        }

        Device device = deviceMapper.selectById(apply.getDeviceId());
        if (device == null) {
            throw new BusinessException("设备不存在");
        }
        if (!Objects.equals(device.getStatus(), 1)) {
            throw new BusinessException("设备当前不可借，不能审批通过");
        }

        Long activeRecordCount = borrowRecordMapper.selectCount(new LambdaQueryWrapper<BorrowRecord>()
                .eq(BorrowRecord::getDeviceId, apply.getDeviceId())
                .in(BorrowRecord::getStatus, 1, 3));
        if (activeRecordCount != null && activeRecordCount > 0) {
            throw new BusinessException("该设备存在未归还记录，不能审批通过");
        }

        BorrowRecord record = new BorrowRecord();
        record.setApplyId(apply.getId());
        record.setDeviceId(apply.getDeviceId());
        record.setUserId(apply.getUserId());
        record.setBorrowTime(LocalDateTime.now());
        record.setStatus(1);
        borrowRecordMapper.insert(record);

        device.setStatus(2);
        deviceMapper.updateById(device);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void returnDevice(ReturnDeviceRequest request) {
        if (request == null || request.getRecordId() == null) {
            throw new BusinessException("借用记录不能为空");
        }

        BorrowRecord record = borrowRecordMapper.selectById(request.getRecordId());
        if (record == null) {
            throw new BusinessException("借用记录不存在");
        }
        if (!Objects.equals(record.getStatus(), 1) && !Objects.equals(record.getStatus(), 3)) {
            throw new BusinessException("该记录不是借用中或逾期状态，不能归还");
        }

        LocalDateTime now = LocalDateTime.now();
        record.setReturnTime(now);
        record.setRemark(request.getRemark());

        BorrowApply apply = borrowApplyMapper.selectById(record.getApplyId());
        if (apply != null && apply.getExpectedReturnTime() != null && now.isAfter(apply.getExpectedReturnTime())) {
            record.setStatus(3);
        } else {
            record.setStatus(2);
        }
        borrowRecordMapper.updateById(record);

        Device device = deviceMapper.selectById(record.getDeviceId());
        if (device != null) {
            device.setStatus(1);
            deviceMapper.updateById(device);
        }
    }

    private Long requireLoginUserId() {
        Long userId = AuthContext.getUserId();
        if (userId == null) {
            throw new BusinessException("用户未登录");
        }
        return userId;
    }
}
