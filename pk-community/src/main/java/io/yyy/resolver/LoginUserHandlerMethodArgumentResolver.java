package io.yyy.resolver;

import io.yyy.annotation.LoginUser;
import io.yyy.entity.UserEntity;
import io.yyy.interceptor.AuthorizationInterceptor;
import io.yyy.service.UserService;
import io.yyy.utils.RedisUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * 有@LoginUser注解的方法参数，注入当前登录用户
 */
@Component
public class LoginUserHandlerMethodArgumentResolver implements HandlerMethodArgumentResolver {


    @Autowired
    private UserService userService;

    @Autowired
    private RedisUtils redisUtils;


    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterType().isAssignableFrom(UserEntity.class) && parameter.hasParameterAnnotation(LoginUser.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer container,
                                  NativeWebRequest request, WebDataBinderFactory factory) {
        // 获取用户ID
        Object object = request.getAttribute(AuthorizationInterceptor.USER_KEY, RequestAttributes.SCOPE_REQUEST);
        if (object == null) {
            return null;
        }
        UserEntity userInfo = redisUtils.get("userId:" + object, UserEntity.class);
        if (userInfo != null) {
            return userInfo;
        }
        // 重新获取用户信息
        UserEntity user = userService.getById((Long) object);
        redisUtils.set("userId:" + object, user, 7200);
        return user;
    }
}
