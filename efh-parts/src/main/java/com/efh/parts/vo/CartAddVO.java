package com.efh.parts.vo;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
public class CartAddVO {

    @NotNull(message = "配件ID不能为空")
    private Long partsId;

    @NotNull(message = "数量不能为空")
    @Min(value = 1, message = "数量至少为1")
    private Integer quantity;
}
