package com.yzy.usercenter.service;

import com.yzy.usercenter.model.domain.User;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.servlet.http.HttpServletRequest;

/**
* @author yzy
* @description 针对表【user(用户)】的数据库操作Service
*/
public interface UserService extends IService<User> {


    /**
     * 用户注册
     * @param userAccount 用户账户
     * @param userPassword 账户密码
     * @param checkPassword 校验密码
     * @param code 编号
     * @return 新用户id
     */
    long userRegister(String userAccount,String userPassword,String checkPassword,String code);

    /**
     * @param userAccount
     * @param userPassword
     * @param request
     * @return 返回脱敏后的用户信息
     */
    User userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 用户脱敏
     * @param originUser
     * @return
     */
    User getSafetyUser(User originUser);

    /**
     * 用户注销
     * @param request
     * @return
     */
    int userLogout(HttpServletRequest request);

    /**
     * 修改用户信息
     * @param code
     * @param userAccount
     * @param userPassword
     * @param isDelete
     * @return
     */
    User userModify(String code,String userAccount, String userPassword,int isDelete);

    /**
     * 修改用户权限
     * @param code
     * @return
     */
    User userModifyAdmin(String code,int userRole);
}
