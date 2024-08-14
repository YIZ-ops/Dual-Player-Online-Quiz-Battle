package io.yyy.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class AppUserResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 用户id
     */
    private Integer uid;
    /**
     * 手机号
     */
    private String mobile;
    /**
     * 用户名
     */
    private String username;

    /**
     * 头像
     */
    private String avatar;
    /**
     * 性别(0未知，1男，2女)
     */
    private Integer gender;

    /**
     * 个性签名
     */
    private String intro;

}
