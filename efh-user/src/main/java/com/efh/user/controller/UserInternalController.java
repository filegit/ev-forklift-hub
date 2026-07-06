package com.efh.user.controller;

import com.efh.common.result.Result;
import com.efh.user.entity.User;
import com.efh.user.entity.UserPoints;
import com.efh.user.service.UserPointsService;
import com.efh.user.service.UserService;
import com.efh.user.vo.PointsAddVO;
import com.efh.user.vo.PointsConsumeVO;
import com.efh.user.vo.UserBriefVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/internal")
public class UserInternalController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserPointsService userPointsService;

    @GetMapping("/user/brief")
    public Result<UserBriefVO> getUserBrief(@RequestHeader(value = "X-User-Id", required = false) String userIdHeader) {
        Long userId = userIdHeader != null ? Long.parseLong(userIdHeader) : null;
        if (userId == null) {
            return Result.error(401, "未授权");
        }
        User user = userService.getById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }
        UserPoints points = userPointsService.getUserPoints(userId);
        UserBriefVO vo = new UserBriefVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setUserType(user.getUserType());
        vo.setAvailablePoints(points.getAvailablePoints());
        return Result.success(vo);
    }

    @PostMapping("/points/consume")
    public Result<Void> consumePoints(@RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
                                      @Validated @RequestBody PointsConsumeVO vo) {
        Long userId = userIdHeader != null ? Long.parseLong(userIdHeader) : null;
        if (userId == null) {
            return Result.error(401, "未授权");
        }
        userPointsService.consumePoints(userId, vo.getPoints(), vo.getReason());
        return Result.success(null);
    }

    @PostMapping("/points/add")
    public Result<Void> addPoints(@RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
                                  @Validated @RequestBody PointsAddVO vo) {
        Long userId = userIdHeader != null ? Long.parseLong(userIdHeader) : null;
        if (userId == null) {
            return Result.error(401, "未授权");
        }
        userPointsService.addPoints(userId, vo.getPoints(), vo.getReason());
        return Result.success(null);
    }
}
