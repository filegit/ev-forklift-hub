package com.efh.user.vo;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class PointsAddVO {
    @NotNull(message = "积分不能为空")
    @Min(value = 1, message = "积分必须大于0")
    private Integer points;

    @NotBlank(message = "原因不能为空")
    private String reason;
}
