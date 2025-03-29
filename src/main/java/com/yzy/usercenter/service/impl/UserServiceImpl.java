package com.yzy.usercenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.Mapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yzy.usercenter.common.ErrorCode;
import com.yzy.usercenter.exception.BusinessException;
import com.yzy.usercenter.service.UserService;
import com.yzy.usercenter.model.domain.User;
import com.yzy.usercenter.mapper.UserMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.yzy.usercenter.constant.UserConstant.USER_LOGIN_STATE;

/**
* @author yzy
 * 用户服务实现类
* @description 针对表【user(用户)】的数据库操作Service实现
* @createDate 2024-11-16 16:36:02
*/
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService {

    @Resource
    private UserMapper userMapper;
    /**
     * 盐值，加密密码
     */
    private static final String SALT="yzy";
    @Autowired
    private Mapper mapper;

    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword,String code,String userName) {
        //1.校验
        if(StringUtils.isAllBlank(userAccount,userPassword,checkPassword,code)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数为空");
        }
        if(userAccount.length()<4){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户账号过短");
        }
        if(userPassword.length()<8||checkPassword.length()<8){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户密码过短");
        }
        if(code.length()>5){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"编号过长");
        }
        if(userName.length()>12){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户名过长");
        }
        if(userName.length()<2){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户名过短");
        }
        //校验账户不能包含特殊字符
        String validPattern="[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if(matcher.find()){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账号不能包含特殊字符");
        }
        if(!userPassword.equals(checkPassword)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"两次密码输入不一致");
        }
        //账户不能重复
        QueryWrapper<User> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("userAccount",userAccount);
        long count=userMapper.selectCount(queryWrapper);
        if(count > 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账号重复");
        }
        //编号不能重复
        if(checkCode(code)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"编号不能重复");
        }
        //2.加密
        String encryptPassword=DigestUtils.md5DigestAsHex((SALT+userPassword).getBytes());
        //3.插入数据
        User user=new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        user.setCode(code);
        user.setUsername(userName);
        boolean saveResult = this.save(user);
        if(!saveResult){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"该用户已注册");
        }
        return user.getId();
    }

    @Override
    public User userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.NULL_ERROR,"账号和密码不能为空");

        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户账号过短");
        }
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户密码过短");
        }
        // 账户不能包含特殊字符
        String validPattern = "[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (matcher.find()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户账号不能包含特殊字符");
        }
        // 2. 加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        // 查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encryptPassword);
        User user = userMapper.selectOne(queryWrapper);
        // 用户不存在
        System.out.println("SQL: " + queryWrapper.getCustomSqlSegment());
        if (user == null) {
            log.info("user login failed, userAccount cannot match userPassword");
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"该用户不存在");
        }
        // 3. 用户脱敏
        User safetyUser = getSafetyUser(user);
        // 4. 记录用户的登录态
        request.getSession().setAttribute(USER_LOGIN_STATE, safetyUser);
        return safetyUser;
    }

    /**
     * 用户脱敏
     * @param originUser
     * @return
     */
    @Override
    public User getSafetyUser(User originUser){
        User safetyUser=new User();
        safetyUser.setId(originUser.getId());
        safetyUser.setUsername(originUser.getUsername());
        safetyUser.setUserAccount(originUser.getUserAccount());
        safetyUser.setAvatarUrl(originUser.getAvatarUrl());
        safetyUser.setGender(originUser.getGender());
        safetyUser.setPhone(originUser.getPhone());
        safetyUser.setEmail(originUser.getEmail());
        safetyUser.setCode(originUser.getCode());
        safetyUser.setUserRole(originUser.getUserRole());
        safetyUser.setUserStatus(originUser.getUserStatus());
        safetyUser.setCreateTime(originUser.getCreateTime());
        return safetyUser;
    }

    /**
     * 用户注销
     * @param request
     */
    @Override
    public int userLogout(HttpServletRequest request) {
        //移除登录态
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return 1;
    }

    @Override
    public User userModify(String code,String userAccount, String userPassword, int isDelete) {
        //校验
        if(StringUtils.isAllBlank(userAccount,userPassword,code)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数为空");
        }
        if(userAccount.length()<4){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户账号过短");
        }
        if(isDelete!=0&&isDelete!=1){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户是否删除状态非法");
        }
        if(code.length()>5){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"编号过长");
        }
        //校验账户不能包含特殊字符
        String validPattern="[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if(matcher.find()){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账号不能包含特殊字符");
        }
        QueryWrapper<User> queryWrapper=new QueryWrapper<>();
        //编号必须存在
        if(!checkCode(code)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"编号不存在");
        }
        //根据code查询id
        long id=userMapper.selectOne(queryWrapper.eq("code",code))
                            .getId();
        //加密
        String encryptPassword=DigestUtils.md5DigestAsHex((SALT+userPassword).getBytes());
        //修改数据
        User user=new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        user.setIsDelete(isDelete);
        user.setId(id);
        boolean updateResult = this.updateById(user);
        if(!updateResult){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"修改失败");
        }
        return user;
    }

    @Override
    public User userModifyAdmin(String code,int userRole) {
        QueryWrapper<User> queryWrapper=new QueryWrapper<>();
        //编号必须存在
        if(!checkCode(code)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"编号不存在");
        }
        //权限必须合法
        if(userRole!=0&&userRole!=1){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"权限不合法");
        }
        //根据code查询id
        long id=userMapper.selectOne(queryWrapper.eq("code",code))
                .getId();
        //修改权限
        User user=new User();
        user.setId(id);
        user.setUserRole(userRole);
        boolean updateResult = this.updateById(user);
        if(!updateResult){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"修改失败");
        }
        return user;
    }

    public boolean checkCode(String code){
        QueryWrapper<User> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("code",code);
        long count=userMapper.selectCount(queryWrapper);
        if(count == 0){
            return false;
        }else {
            return true;
        }
    };

}




