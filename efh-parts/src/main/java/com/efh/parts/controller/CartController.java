package com.efh.parts.controller;



import com.efh.common.result.Result;

import com.efh.common.utils.UserContextUtil;

import com.efh.parts.service.CartService;

import com.efh.parts.vo.CartAddVO;

import com.efh.parts.vo.CartItemVO;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.validation.annotation.Validated;

import org.springframework.web.bind.annotation.*;



import java.util.HashMap;

import java.util.List;

import java.util.Map;



@RestController

@RequestMapping("/api/parts/cart")

public class CartController {



    @Autowired

    private CartService cartService;



    @GetMapping

    public Result<List<CartItemVO>> list(@RequestHeader(value = "X-User-Id", required = false) String userIdHeader) {

        Long userId = UserContextUtil.requireUserId(userIdHeader);

        return Result.success(cartService.listCart(userId));

    }



    @GetMapping("/count")

    public Result<Map<String, Integer>> count(@RequestHeader(value = "X-User-Id", required = false) String userIdHeader) {

        Long userId = UserContextUtil.requireUserId(userIdHeader);

        Map<String, Integer> map = new HashMap<>();

        map.put("count", cartService.countCart(userId));

        return Result.success(map);

    }



    @PostMapping

    public Result<?> add(@RequestHeader(value = "X-User-Id", required = false) String userIdHeader,

                           @Validated @RequestBody CartAddVO vo) {

        Long userId = UserContextUtil.requireUserId(userIdHeader);

        cartService.addToCart(userId, vo);

        return Result.success();

    }



    @PutMapping("/{partsId}")

    public Result<?> update(@RequestHeader(value = "X-User-Id", required = false) String userIdHeader,

                            @PathVariable Long partsId,

                            @RequestParam Integer quantity) {

        Long userId = UserContextUtil.requireUserId(userIdHeader);

        cartService.updateQuantity(userId, partsId, quantity);

        return Result.success();

    }



    @DeleteMapping("/{partsId}")

    public Result<?> remove(@RequestHeader(value = "X-User-Id", required = false) String userIdHeader,

                            @PathVariable Long partsId) {

        Long userId = UserContextUtil.requireUserId(userIdHeader);

        cartService.removeItem(userId, partsId);

        return Result.success();

    }

}

