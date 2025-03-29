package com.yzy.usercenter.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yzy.usercenter.common.BaseResponse;
import com.yzy.usercenter.common.ErrorCode;
import com.yzy.usercenter.common.ResultUtils;
import com.yzy.usercenter.exception.BusinessException;
import com.yzy.usercenter.model.domain.User;
import com.yzy.usercenter.model.domain.request.UserLoginRequest;
import com.yzy.usercenter.model.domain.request.UserModifyAdminRequest;
import com.yzy.usercenter.model.domain.request.UserModifyRequest;
import com.yzy.usercenter.model.domain.request.UserRegisterRequest;
import com.yzy.usercenter.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

import static com.yzy.usercenter.constant.UserConstant.ADMIN_ROLE;
import static com.yzy.usercenter.constant.UserConstant.USER_LOGIN_STATE;

/**
 * 用户接口
 */
@RestController
@RequestMapping("user")
public class UserController {
    @Resource
    private UserService userService;
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest){
        if(userRegisterRequest==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount=userRegisterRequest.getUserAccount();
        String userPassword=userRegisterRequest.getUserPassword();
        String checkPassword=userRegisterRequest.getCheckPassword();
        String userName=userRegisterRequest.getUserName();
        String code=userRegisterRequest.getCode();
        if(StringUtils.isAnyBlank(userAccount,userPassword,checkPassword,code)){
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        long result = userService.userRegister(userAccount, userPassword, checkPassword,code,userName);
        return ResultUtils.success(result );
    }

    @PostMapping("/login")
    public BaseResponse<User> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request){
        if(userLoginRequest==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount=userLoginRequest.getUserAccount();
        String userPassword=userLoginRequest.getUserPassword();
        if(StringUtils.isAnyBlank(userAccount,userPassword)){
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        User user=userService.userLogin(userAccount, userPassword,request);
        return ResultUtils.success(user);
    }

    @PostMapping("/logout")
    public BaseResponse<Integer> userLogout(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request){
        if(userLoginRequest==null){
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        Integer result=userService.userLogout(request);
        return ResultUtils.success(result);
    }

    @GetMapping("/current")
    public BaseResponse<User> getCurrentUser(HttpServletRequest request){
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if(currentUser ==null){
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        long userId=currentUser.getId();
        User user=userService.getById(userId);
        User safetyUser= userService.getSafetyUser(user);
        return ResultUtils.success(safetyUser);
    }

    @GetMapping("/search")
    public BaseResponse<List<User>> searchUsers(@RequestParam String username, HttpServletRequest request) {
        if (!isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotBlank(username)) {
            queryWrapper.like("username", username);
        }
        List<User> userList = userService.list(queryWrapper);
        List<User> list = userList.stream()
                .map(user -> userService.getSafetyUser(user))
                .collect(Collectors.toList());
        return ResultUtils.success(list);
    }

    @GetMapping("/modify")
    public BaseResponse<User> modifyUser(@RequestBody UserModifyRequest userModifyRequest){
        if(userModifyRequest==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String code=userModifyRequest.getCode();
        String userAccount=userModifyRequest.getUserAccount();
        String userPassword=userModifyRequest.getUserPassword();
        int isDelete=userModifyRequest.getIsDelete();
        if(StringUtils.isAnyBlank(userAccount,userPassword)){
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        User user=userService.userModify(code,userAccount,userPassword,isDelete);
        return ResultUtils.success(user);
    }

    @GetMapping("/modifyadmin")
    public BaseResponse<User> modifyAdmin(@RequestBody UserModifyAdminRequest userModifyAdminRequest){
        if(userModifyAdminRequest==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String code=userModifyAdminRequest.getCode();
        int userRole=userModifyAdminRequest.getUserRole();
        if(StringUtils.isBlank(code)){
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        User user=userService.userModifyAdmin(code,userRole);
        return ResultUtils.success(user);
    }


    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteUsers(@RequestParam int id,HttpServletRequest request){
        if(!isAdmin(request)){
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        if(id<=0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Boolean b= userService.removeById(id);
        return ResultUtils.success(b);
    }

    /**
     * 是否为管理员
     * @param request
     * @return
     */
    private boolean isAdmin(HttpServletRequest request){
        //仅管理员可查询
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User user=(User) userObj;
        if(user== null || user.getUserRole() != ADMIN_ROLE){
            return false;
        }
        return true;
    }
}
