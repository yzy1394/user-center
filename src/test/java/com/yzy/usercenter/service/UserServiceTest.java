package com.yzy.usercenter.service;

import org.junit.jupiter.api.Assertions;
import org.springframework.boot.test.context.SpringBootTest;
import org.junit.jupiter.api.Test;
import com.yzy.usercenter.model.domain.User;


import javax.annotation.Resource;
@SpringBootTest
public class UserServiceTest {
    @Resource
    private UserService userService;

    @Test
    public void testAddUser(){
        User user=new User();
        user.setUsername("yuan");
        user.setUserAccount("123");
        user.setAvatarUrl("https://static.nowcoder.com/fe/file/logo/1.png");
        user.setGender(0);
        user.setUserPassword("xxx");
        user.setPhone("123");
        user.setEmail("456");
        boolean result=userService.save(user);
        System.out.println(user.getId());
        Assertions.assertEquals(true,result);
    }

    @Test
    void userRegister() {
        String userAccount="yuan";
        String userPassword="12345678";
        String checkPassword="12345678";
        String code="2";
        String userName="yuan";
        long result = userService.userRegister(userAccount, userPassword, checkPassword,code,userName);
//        Assertions.assertEquals(-1,result);
//        userAccount="y";
//        result = userService.userRegister(userAccount, userPassword, checkPassword,code);
//        Assertions.assertEquals(-1,result);
//        userAccount="yuan";
//        userPassword="123456";
//        result = userService.userRegister(userAccount, userPassword, checkPassword,code);
//        Assertions.assertEquals(-1,result);
//        userAccount="y uan";
//        userPassword="12345678";
//        result = userService.userRegister(userAccount, userPassword, checkPassword,code);
//        Assertions.assertEquals(-1,result);
//        checkPassword="123456789";
//        result = userService.userRegister(userAccount, userPassword, checkPassword,code);
//        Assertions.assertEquals(-1,result);
//        userAccount="123";
//        checkPassword="12345678";
//        result = userService.userRegister(userAccount, userPassword, checkPassword,code);
//        Assertions.assertEquals(-1,result);
//        userAccount="yuan";
        Assertions.assertTrue(result>0);
    }
}