package com.hmdp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.dto.LoginFormDTO;
import com.hmdp.dto.Result;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.User;
import com.hmdp.mapper.UserMapper;
import com.hmdp.service.IUserService;
import com.hmdp.utils.RedisConstants;
import com.hmdp.utils.RegexUtils;
import com.hmdp.utils.SystemConstants;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.hmdp.utils.RedisConstants.*;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public Result sendCode(String phone, HttpSession session) {
        // 1. 校验手机号
        if(RegexUtils.isPhoneInvalid(phone)){
            // 2. 如果不符合 返回错误信息
            return Result.fail("手机号格式错误！");
        }
        // 3. 符合 生成验证码
        String code = RandomUtil.randomNumbers(6);
        // 4. 保存验证码到 redis
        redisTemplate.opsForValue().set(LOGIN_CODE_KEY + phone,code,LOGIN_CODE_TTL, TimeUnit.MINUTES);
        // 5. 发送验证码
        log.debug("发送短信验证码成功，验证码:{}",code);
        // 返回 ok
        return Result.ok();
    }

    @Override
    public Result login(LoginFormDTO loginForm, HttpSession session) {
        // 1. 校验手机号
        String phone = loginForm.getPhone();
        log.info("校验手机号，{}",phone);
        if(RegexUtils.isPhoneInvalid(phone)){
            // 2. 如果不符合，返回错误信息
            log.info("手机号格式错误，{}",phone);
            return Result.fail("手机号格式错误！");
        }
        // TODO 3.从 redis获取验证码并校验
        String cacheCode = redisTemplate.opsForValue().get(LOGIN_CODE_KEY + phone);
        String code = loginForm.getCode();
        if (cacheCode == null || !cacheCode.equals(code)){
            //  不一致，报错
            log.info("验证码式错误，{}",code);
            return Result.fail("验证码错误");
        }
        // 4. 一致，根据手机号查询用户 select * from tb_user where phone = ?
        User user = query().eq("phone", phone).one();
        // 5. 判断用户是否存在
        if(user == null){
            // 6. 不存在，创建用户并保存
            user = createUserWithPhone(phone);
            log.info("用户不存在，创建用户，{}",user.toString());
        }
        //  7. 保存用户信息到 redis
        //  7.1 随机生成token 作为登录令牌
        String token = UUID.randomUUID().toString(true);
        //  7.2 将 user 对象转换为 hashMap 去存储
        UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
        //  7.3 存储
        String tokenKey = LOGIN_USER_KEY;
        Map<String,Object> userMap = BeanUtil.beanToMap(userDTO,new HashMap<>(), CopyOptions.create()
                .setIgnoreNullValue(true).setFieldValueEditor((filedName,filedValue)->filedValue.toString()));
        // 定时销毁
        redisTemplate.opsForHash().putAll( tokenKey + token,userMap);
        redisTemplate.expire(tokenKey,LOGIN_USER_TTL,TimeUnit.MINUTES);
        // 8 返回 token
        return Result.ok(token);
    }

    private User createUserWithPhone(String phone) {
        // 1. 创建用户
        User user = new User();
        user.setPhone(phone);
        user.setNickName(SystemConstants.USER_NICK_NAME_PREFIX + RandomUtil.randomString(10));
        // 2. 保存用户
        save(user);
        return user;
    }
}
