package com.yzy.usercenter.model.domain.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户注册请求
 *
 * @author yzy
 */
@Data
public class UserRegisterRequest implements Serializable {
    private static final long serialVersionUId=3191241716373120793L;

    private String userAccount;

    private String userPassword;

    private String checkPassword;

    private String code;
}
