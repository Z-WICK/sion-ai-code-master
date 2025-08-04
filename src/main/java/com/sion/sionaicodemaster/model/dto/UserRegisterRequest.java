package com.sion.sionaicodemaster.model.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author : wick
 * @Date : 2025/8/2 22:36
 */
@Data
public class UserRegisterRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 密码
     */
    private String userPassword;

    /**
     * 确认密码
     */
    private String checkPassword;
}
