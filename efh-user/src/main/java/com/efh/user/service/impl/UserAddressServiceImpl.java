package com.efh.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.efh.common.exception.BusinessException;
import com.efh.user.entity.UserAddress;
import com.efh.user.mapper.UserAddressMapper;
import com.efh.user.service.UserAddressService;
import com.efh.user.vo.AddressVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserAddressServiceImpl extends ServiceImpl<UserAddressMapper, UserAddress> implements UserAddressService {

    @Override
    public List<UserAddress> listByUser(Long userId) {
        return list(new LambdaQueryWrapper<UserAddress>()
                .eq(UserAddress::getUserId, userId)
                .orderByDesc(UserAddress::getIsDefault)
                .orderByDesc(UserAddress::getUpdateTime));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserAddress addAddress(Long userId, AddressVO vo) {
        if (vo.getIsDefault() != null && vo.getIsDefault() == 1) {
            clearDefault(userId);
        }
        UserAddress address = toEntity(userId, vo);
        if (count(new LambdaQueryWrapper<UserAddress>().eq(UserAddress::getUserId, userId)) == 0) {
            address.setIsDefault(1);
        }
        save(address);
        return address;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAddress(Long userId, Long id, AddressVO vo) {
        UserAddress address = getAndCheck(userId, id);
        address.setReceiverName(vo.getReceiverName());
        address.setPhone(vo.getPhone());
        address.setProvince(vo.getProvince());
        address.setCity(vo.getCity());
        address.setDistrict(vo.getDistrict());
        address.setDetail(vo.getDetail());
        if (vo.getIsDefault() != null && vo.getIsDefault() == 1) {
            clearDefault(userId);
            address.setIsDefault(1);
        }
        updateById(address);
    }

    @Override
    public void deleteAddress(Long userId, Long id) {
        UserAddress address = getAndCheck(userId, id);
        removeById(address.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setDefault(Long userId, Long id) {
        getAndCheck(userId, id);
        clearDefault(userId);
        update(new LambdaUpdateWrapper<UserAddress>()
                .eq(UserAddress::getId, id)
                .set(UserAddress::getIsDefault, 1));
    }

    @Override
    public UserAddress getDefault(Long userId) {
        UserAddress address = getOne(new LambdaQueryWrapper<UserAddress>()
                .eq(UserAddress::getUserId, userId)
                .eq(UserAddress::getIsDefault, 1)
                .last("LIMIT 1"));
        if (address == null) {
            address = getOne(new LambdaQueryWrapper<UserAddress>()
                    .eq(UserAddress::getUserId, userId)
                    .orderByDesc(UserAddress::getUpdateTime)
                    .last("LIMIT 1"));
        }
        return address;
    }

    @Override
    public String formatFullAddress(UserAddress address) {
        if (address == null) {
            return "";
        }
        return String.format("%s%s%s%s",
                nullToEmpty(address.getProvince()),
                nullToEmpty(address.getCity()),
                nullToEmpty(address.getDistrict()),
                nullToEmpty(address.getDetail()));
    }

    private UserAddress getAndCheck(Long userId, Long id) {
        UserAddress address = getById(id);
        if (address == null || !address.getUserId().equals(userId)) {
            throw new BusinessException("地址不存在");
        }
        return address;
    }

    private void clearDefault(Long userId) {
        update(new LambdaUpdateWrapper<UserAddress>()
                .eq(UserAddress::getUserId, userId)
                .set(UserAddress::getIsDefault, 0));
    }

    private UserAddress toEntity(Long userId, AddressVO vo) {
        UserAddress address = new UserAddress();
        address.setUserId(userId);
        address.setReceiverName(vo.getReceiverName());
        address.setPhone(vo.getPhone());
        address.setProvince(vo.getProvince());
        address.setCity(vo.getCity());
        address.setDistrict(vo.getDistrict());
        address.setDetail(vo.getDetail());
        address.setIsDefault(vo.getIsDefault() != null ? vo.getIsDefault() : 0);
        return address;
    }

    private String nullToEmpty(String s) {
        return s == null ? "" : s;
    }
}
