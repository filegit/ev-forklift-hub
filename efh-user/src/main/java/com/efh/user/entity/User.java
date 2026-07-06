package com.efh.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.efh.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户实体
 * 字段与数据库表完全对应
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user")
public class User extends BaseEntity {
    
    private String username;      // 用户名
    private String password;      // 密码（BCrypt加密）
    private String nickname;      // 昵称
    private String avatar;        // 头像URL
    private String phone;         // 手机号
    private String email;         // 邮箱
    private Integer gender;       // 性别：0-未知，1-男，2-女
    private Integer userType;     // 用户类型：1-普通用户，2-技师，3-商家，9-管理员
    private Integer status;       // 状态：0-禁用，1-正常
    private Integer points;       // 积分
}
