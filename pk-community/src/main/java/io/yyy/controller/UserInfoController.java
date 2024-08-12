package io.yyy.controller;

import io.yyy.vo.AppUserInfoResponse;
import io.yyy.vo.AppUserResponse;
import io.yyy.utils.R;
import io.yyy.entity.UserEntity;
import io.yyy.vo.AppUserInfoForm;
import io.yyy.vo.AppUserUpdateForm;
import io.yyy.vo.SendCodeForm;
import io.yyy.vo.SmsLoginForm;
import io.yyy.service.UserService;
import io.yyy.annotation.Login;
import io.yyy.annotation.LoginUser;
import io.yyy.utils.JwtUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/app/user")
public class UserInfoController {


    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserService appUserService;

    @PostMapping("/sendSmsCode")
    public R sendSmsCode(@RequestBody SendCodeForm param) {
        String code = appUserService.sendSmsCode(param);
        return R.ok("测试验证码:" + code);
    }


    /**
     * 手机验证码登录
     */
    @PostMapping("/smsLogin")
    public R smsLogin(@RequestBody SmsLoginForm form, HttpServletRequest request) {
        // 用户登录
        Integer userId = null;
        try {
            userId = appUserService.smsLogin(form, request);
        } catch (Exception e) {
            return R.error(400, e.getMessage());
        }
        // 生成token
        String token = jwtUtils.generateToken(userId);
        Map<String, Object> map = new HashMap<>();
        map.put("token", token);
        map.put("expire", jwtUtils.getExpire());
        return R.ok(map);
    }


    @Login
    @GetMapping("/userInfo")
    public R userInfo(@LoginUser UserEntity user) {
        AppUserResponse response = appUserService.getUserInfo(user);
        return R.ok().put("result", response);
    }


    @Login
    @PostMapping("/userInfoEdit")
    public R userInfoEdit(@LoginUser UserEntity user, @RequestBody AppUserUpdateForm appUserUpdateForm) {
        appUserService.updateAppUserInfo(appUserUpdateForm, user);
        return R.ok("修改成功");
    }


    @Login
    @PostMapping("/userInfoById")
    public R userInfoById(@RequestBody AppUserInfoForm request,
                          @LoginUser UserEntity user) {
        AppUserInfoResponse response = appUserService.findUserInfoById(request.getUid(), user);
        return R.ok().put("result", response);
    }


}
