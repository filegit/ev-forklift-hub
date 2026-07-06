package com.efh.parts.vo;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class OrderSubmitVO {

    private List<Long> cartItemIds;
    private List<DirectItem> directItems;

    private Long addressId;

    @NotBlank(message = "收货人不能为空")
    private String receiverName;

    @NotBlank(message = "联系电话不能为空")
    private String receiverPhone;

    @NotBlank(message = "收货地址不能为空")
    private String receiverAddress;

    private String remark;

    @Data
    public static class DirectItem {
        @NotNull
        private Long partsId;
        @NotNull
        @Min(1)
        private Integer quantity;
    }
}
