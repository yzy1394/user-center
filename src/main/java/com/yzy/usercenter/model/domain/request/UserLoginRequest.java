package com.yzy.usercenter.model.domain.request;

import lombok.Data;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserLoginRequest implements Serializable {
    //盐值
    private static final long serialVersionUID =3191241716373120793L;

    // 用户账号
    private String userAccount;

    // 用户密码
    private String userPassword;
}
