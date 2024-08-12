package io.yyy.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import io.yyy.utils.*;
import io.yyy.vo.*;
import io.yyy.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.yyy.dao.UserDao;
import io.yyy.entity.UserEntity;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;


@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserDao, UserEntity> implements UserService {

    @Autowired
    private RedisUtils redisUtils;

    /**
     * 注册/登录
     *
     * @param form    手机验证码登录dto
     * @param request
     * @return 用户ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer smsLogin(SmsLoginForm form, HttpServletRequest request) {
        UserEntity appUserEntity = this.lambdaQuery().eq(UserEntity::getMobile, form.getMobile()).one();
        String codeKey = Constant.SMS_PREFIX + form.getMobile();
        String s = redisUtils.get(codeKey);
        if (io.yyy.utils.ObjectUtil.isEmpty(s)) {
            throw new RuntimeException("请先获取验证码");
        }
        if (!s.equals(form.getCode())) {
            throw new RuntimeException("验证码错误");
        }
        if (ObjectUtil.isNotNull(appUserEntity)) {
            return appUserEntity.getUid();
        } else {
            //注册
            UserEntity appUser = new UserEntity();
            appUser.setMobile(form.getMobile());
            appUser.setGender(0);
            appUser.setAvatar(Constant.DEAULT_HEAD);
            appUser.setUsername(generateRandomName());
            appUser.setCreateTime(DateUtil.nowDateTime());
            appUser.setUpdateTime(DateUtil.nowDateTime());
            baseMapper.insert(appUser);
            UserEntity user = this.lambdaQuery()
                    .eq(UserEntity::getMobile, form.getMobile())
                    .one();
            if (ObjectUtil.isNull(user)) {
                throw new RuntimeException("注册失败");
            }
            //其他业务处理
            return user.getUid();
        }
    }

    @Override
    public String sendSmsCode(SendCodeForm param) {
        String code = RandomUtil.randomNumbers(Constant.SMS_SIZE);
        String codeKey = Constant.SMS_PREFIX + param.getMobile();
        String s = redisUtils.get(codeKey);
        if (ObjectUtil.isNotNull(s)) {
            return s;
        }
        redisUtils.set(codeKey, code, 60 * 5);
        return code;
    }

    @Override
    public AppUserResponse getUserInfo(UserEntity user) {
        AppUserResponse response = new AppUserResponse();
        BeanUtils.copyProperties(user, response);
        return response;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAppUserInfo(AppUserUpdateForm appUserUpdateForm, UserEntity user) {
        if (!ObjectUtil.isEmpty(appUserUpdateForm.getAvatar())) {
            user.setAvatar(appUserUpdateForm.getAvatar());
        }
        if (!ObjectUtil.isEmpty(appUserUpdateForm.getGender())) {
            user.setGender(appUserUpdateForm.getGender());
        }
        if (!ObjectUtil.isEmpty(appUserUpdateForm.getUsername())) {
            user.setUsername(appUserUpdateForm.getUsername());
        }
        if (!ObjectUtil.isEmpty(appUserUpdateForm.getIntro())) {
            user.setIntro(appUserUpdateForm.getIntro());
        }
        baseMapper.updateById(user);
        redisUtils.delete(RedisKeys.getUserKey(user.getUid()));
    }

    @Override
    public AppUserInfoResponse findUserInfoById(Integer uid, UserEntity user) {
        UserEntity userEntity = this.getById(uid);
        if (ObjectUtil.isNull(userEntity)) {
            throw new RuntimeException("用户不存在");
        }
        AppUserInfoResponse response = new AppUserInfoResponse();
        BeanUtils.copyProperties(userEntity, response);
        return response;
    }

    /**
     * 生成随机用户名
     *
     * @return 随机用户名
     */
    private String generateRandomName() {
        String name = "Y_" + RandomUtil.randomNumbers(8);
        return name;
    }

}
