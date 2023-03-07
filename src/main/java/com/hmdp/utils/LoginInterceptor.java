package com.hmdp.utils;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.User;
import lombok.val;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @Author LiuJixue
 * @Date 2023/3/7 16:55
 * @PackageName:com.hmdp.utils
 * @ClassName: loginInterceptor
 * @Description: 拦截器，前置拦截器，用户请求进入时拦截。后置处理用户销毁用户信息
 * @Version 1.0
 */
public class LoginInterceptor implements HandlerInterceptor {

    private StringRedisTemplate stringRedisTemplate;

    public LoginInterceptor(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1. 获取请求头中的 token
        String token = request.getHeader("authorization");
        if(StrUtil.isBlank(token)){
            // 4. 不存在，拦截,返回 401 状态码，未授权
            response.setStatus(401);
            return false;
        }
        // 2. 基于 token 获取redis中的用户
        String key = RedisConstants.LOGIN_USER_KEY + token ;
        Map<Object,Object> userMap = stringRedisTemplate.opsForHash().entries(key);
        // 3. 判断用户是否存在
        if (userMap.isEmpty()){
            // 4. 不存在，拦截,返回 401 状态码，未授权
            response.setStatus(401);
            return false;
        }
        // 5. 将查询到的hash 数据转为userDTO对象
        UserDTO userDTO = BeanUtil.fillBeanWithMap(userMap, new UserDTO(), false);
        // 6. 存在，保存用户信息到ThreadLocal
        UserHolder.saveUser((userDTO));
        // 7. 刷新token 有效期
        stringRedisTemplate.expire(key,RedisConstants.LOGIN_USER_TTL, TimeUnit.MINUTES);
        // 8. 放行
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 移除用户
        UserHolder.removeUser();
    }
}
