package com.efh.user.controller;



import com.efh.common.result.Result;

import com.efh.common.utils.UserContextUtil;

import com.efh.user.entity.UserAddress;

import com.efh.user.service.UserAddressService;

import com.efh.user.vo.AddressVO;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.validation.annotation.Validated;

import org.springframework.web.bind.annotation.*;



import java.util.List;



@RestController

@RequestMapping("/api/address")

public class UserAddressController {



    @Autowired

    private UserAddressService addressService;



    @GetMapping("/list")

    public Result<List<UserAddress>> list(@RequestHeader(value = "X-User-Id", required = false) String userIdHeader) {

        Long userId = UserContextUtil.requireUserId(userIdHeader);

        return Result.success(addressService.listByUser(userId));

    }



    @GetMapping("/default")

    public Result<UserAddress> getDefault(@RequestHeader(value = "X-User-Id", required = false) String userIdHeader) {

        Long userId = UserContextUtil.requireUserId(userIdHeader);

        return Result.success(addressService.getDefault(userId));

    }



    @PostMapping

    public Result<UserAddress> add(@RequestHeader(value = "X-User-Id", required = false) String userIdHeader,

                                   @Validated @RequestBody AddressVO vo) {

        Long userId = UserContextUtil.requireUserId(userIdHeader);

        return Result.success(addressService.addAddress(userId, vo));

    }



    @PutMapping("/{id}")

    public Result<?> update(@RequestHeader(value = "X-User-Id", required = false) String userIdHeader,

                            @PathVariable Long id,

                            @Validated @RequestBody AddressVO vo) {

        Long userId = UserContextUtil.requireUserId(userIdHeader);

        addressService.updateAddress(userId, id, vo);

        return Result.success();

    }



    @DeleteMapping("/{id}")

    public Result<?> delete(@RequestHeader(value = "X-User-Id", required = false) String userIdHeader,

                            @PathVariable Long id) {

        Long userId = UserContextUtil.requireUserId(userIdHeader);

        addressService.deleteAddress(userId, id);

        return Result.success();

    }



    @PostMapping("/{id}/default")

    public Result<?> setDefault(@RequestHeader(value = "X-User-Id", required = false) String userIdHeader,

                                @PathVariable Long id) {

        Long userId = UserContextUtil.requireUserId(userIdHeader);

        addressService.setDefault(userId, id);

        return Result.success();

    }

}

