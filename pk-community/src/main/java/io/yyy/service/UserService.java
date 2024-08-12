package io.yyy.service;

import com.baomidou.mybatisplus.extension.service.IService;
import io.yyy.entity.UserEntity;
import io.yyy.vo.AppUserInfoResponse;
import io.yyy.vo.AppUserResponse;
import io.yyy.vo.AppUserUpdateForm;
import io.yyy.vo.SendCodeForm;
import io.yyy.vo.SmsLoginForm;

import javax.servlet.http.HttpServletRequest;

public interface UserService extends IService<UserEntity> {

    Integer smsLogin(SmsLoginForm form, HttpServletRequest request);

    String sendSmsCode(SendCodeForm param);

    AppUserResponse getUserInfo(UserEntity user);

    void updateAppUserInfo(AppUserUpdateForm appUserUpdateForm, UserEntity user);

    AppUserInfoResponse findUserInfoById(Integer uid, UserEntity user);

}

