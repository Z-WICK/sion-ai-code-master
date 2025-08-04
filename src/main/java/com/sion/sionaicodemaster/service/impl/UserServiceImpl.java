package com.sion.sionaicodemaster.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.sion.sionaicodemaster.exception.BusinessException;
import com.sion.sionaicodemaster.exception.ErrorCode;
import com.sion.sionaicodemaster.exception.ThrowUtils;
import com.sion.sionaicodemaster.mapper.UserMapper;
import com.sion.sionaicodemaster.model.dto.UserQueryRequest;
import com.sion.sionaicodemaster.model.entity.User;
import com.sion.sionaicodemaster.model.enums.UserRoleEnum;
import com.sion.sionaicodemaster.model.vo.LoginUserVO;
import com.sion.sionaicodemaster.model.vo.UserVO;
import com.sion.sionaicodemaster.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.List;
import java.util.stream.Collectors;

import static com.sion.sionaicodemaster.contant.UserConstant.USER_LOGIN_STATE;

/**
 * 用户 服务层实现。
 *
 * @author <a href="https://github.com/Z-WICK">JohnSion</a>
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    /**
     * 用户注册
     *
     * @param userAccount
     * @param userPassword
     * @param checkPassword
     * @return long
     */
    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        // 1.校验
        if (StrUtil.hasBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }

        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码过短");
        }
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次密码不一致");
        }
        // 2.查询用户是否已存在
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("userAccount", userAccount);
        long count = this.mapper.selectCountByQuery(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户已存在");
        }


        // 3.加密
        String encryptPassword = getEncryptPassword(userPassword);


        // 4.创建用户
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        user.setUserName("无名");
        user.setUserRole(UserRoleEnum.USER.getValue());
        boolean saveResult = this.save(user);
        if (!saveResult) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误");
        }
        return user.getId();

    }

    /**
     * 获取登录用户信息（脱敏版）
     *
     * @param user
     * @return {@link LoginUserVO }
     */
    @Override
    public LoginUserVO getLoginUserVO(User user) {
        if (user == null) {
            return null;
        }
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtils.copyProperties(user, loginUserVO);
        return loginUserVO;
    }

    /**
     * 用户登录
     *
     * @param userAccount
     * @param userPassword
     * @param request
     * @return {@link LoginUserVO }
     */
    @Override
    public LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 1.校验
        ThrowUtils.throwIf(StrUtil.hasBlank(userAccount, userPassword), ErrorCode.PARAMS_ERROR, "参数为空");
        ThrowUtils.throwIf(userAccount.length() < 4, ErrorCode.PARAMS_ERROR, "用户账号过短");
        ThrowUtils.throwIf(userPassword.length() < 8, ErrorCode.PARAMS_ERROR, "密码过短");


        // 2.查询用户是否存在

        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", getEncryptPassword(userPassword));
        User user = this.mapper.selectOneByQuery(queryWrapper);

        // 用户不存在
        ThrowUtils.throwIf(user == null, ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");

        // 3. 记录用户的登录状态
        request.getSession().setAttribute(USER_LOGIN_STATE, user);

        // 4. 返回脱敏用户信息

        return this.getLoginUserVO(user);
    }

    /**
     * 获取登录用户信息
     *
     * @param request
     * @return {@link User }
     */
    @Override
    public User getLoginUser(HttpServletRequest request) {
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null || currentUser.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        // 从数据库查询
        long userId = currentUser.getId();
        currentUser = this.getById(userId);
        ThrowUtils.throwIf(currentUser == null, ErrorCode.NOT_LOGIN_ERROR);

        return currentUser;

    }

    /**
     * 注销用户信息
     *
     * @param request
     * @return boolean
     */
    @Override
    public boolean userLogout(HttpServletRequest request) {
        // 先判断是否已登录
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        ThrowUtils.throwIf(userObj == null, ErrorCode.NOT_LOGIN_ERROR, "用户未登录");

        // 移除登录状态
        request.getSession().removeAttribute(USER_LOGIN_STATE);

        return true;
    }

    /**
     * 获取用户信息（脱敏版）
     *
     * @param user
     * @return {@link UserVO }
     */
    @Override
    public UserVO getUserVo(User user) {
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }

    /**
     * 获取用户列表信息（脱敏版）
     *
     * @param userList
     * @return {@link List }<{@link UserVO }>
     */
    @Override
    public List<UserVO> getUserVoList(List<User> userList) {
        ThrowUtils.throwIf(CollUtil.isEmpty(userList), ErrorCode.PARAMS_ERROR, "用户列表为空");
        return userList.stream().map(this::getUserVo).collect(Collectors.toList());
    }

    /**
     * 查询请求转换QueryWrapper对象
     *
     * @param userQueryRequest
     * @return {@link QueryWrapper }
     */
    @Override
    public QueryWrapper getQueryWrapper(UserQueryRequest userQueryRequest) {
        ThrowUtils.throwIf(userQueryRequest == null, ErrorCode.PARAMS_ERROR, "查询参数为空");

        Long id = userQueryRequest.getId();
        String userName = userQueryRequest.getUserName();
        String userAccount = userQueryRequest.getUserAccount();
        String userProfile = userQueryRequest.getUserProfile();
        String userRole = userQueryRequest.getUserRole();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();
        return QueryWrapper.create()
                .eq("id", id)
                .eq("userName", userName)
                .eq("userAccount", userAccount)
                .eq("userProfile", userProfile)
                .eq("userRole", userRole)
                .orderBy(sortField, "ascend".equals(sortOrder));
    }

    /**
     * 混淆加密
     *
     * @param userPassword
     * @return {@link String }
     */
    @Override
    public String getEncryptPassword(String userPassword) {
        // 盐值，混淆密码
        final String SALT = "u&n3@jah*HHsi1";
        return DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
    }
}
