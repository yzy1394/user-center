package com.yzy.usercenter.model.domain.request;

import lombok.Data;

@Data
public class UserModifyAdminRequest {
    //用户编号
    private String code;

    //用户权限
    private int userRole;
}
