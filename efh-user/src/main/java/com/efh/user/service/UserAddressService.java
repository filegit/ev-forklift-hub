package com.efh.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.efh.user.entity.UserAddress;
import com.efh.user.vo.AddressVO;

import java.util.List;

public interface UserAddressService extends IService<UserAddress> {

    List<UserAddress> listByUser(Long userId);

    UserAddress addAddress(Long userId, AddressVO vo);

    void updateAddress(Long userId, Long id, AddressVO vo);

    void deleteAddress(Long userId, Long id);

    void setDefault(Long userId, Long id);

    UserAddress getDefault(Long userId);

    String formatFullAddress(UserAddress address);
}
