package com.efh.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.efh.common.exception.BusinessException;
import com.efh.common.utils.JwtUtil;
import com.efh.user.entity.User;
import com.efh.user.mapper.UserMapper;
import com.efh.user.service.SmsService;
import com.efh.user.service.UserService;
import com.efh.user.vo.LoginVO;
import com.efh.user.vo.RegisterVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 用户服务实现类
 * 功能：用户注册、登录、信息管理
 * 技术：ShardingSphere分库、Redis缓存、BCrypt密码加密、JWT认证
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private SmsService smsService;

    // BCrypt 密码加密器
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // Redis 缓存键前缀
    private static final String USER_CACHE_PREFIX = "user:info:";
    private static final int CACHE_EXPIRE_MINUTES = 30;

    /**
     * 用户注册
     * 
     * @param vo 注册信息
     * @return JWT Token
     */
    @Override
    public String register(RegisterVO vo) {
        log.info("用户注册开始: username={}", vo.getUsername());

        // 1. 检查用户名是否已存在
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, vo.getUsername());
        User existUser = this.getOne(wrapper);
        
        if (existUser != null) {
            log.warn("用户名已存在: {}", vo.getUsername());
            throw new BusinessException("用户名已存在");
        }

        // 2. 检查手机号是否已存在（如果提供了手机号）
        if (vo.getPhone() != null && !vo.getPhone().isEmpty()) {
            LambdaQueryWrapper<User> phoneWrapper = new LambdaQueryWrapper<>();
            phoneWrapper.eq(User::getPhone, vo.getPhone());
            User existPhone = this.getOne(phoneWrapper);
            
            if (existPhone != null) {
                log.warn("手机号已存在: {}", vo.getPhone());
                throw new BusinessException("手机号已被注册");
            }
        }

        // 3. 创建用户对象
        User user = new User();
        user.setUsername(vo.getUsername());
        // 使用 BCrypt 加密密码
        user.setPassword(passwordEncoder.encode(vo.getPassword()));
        user.setNickname(vo.getNickname());
        user.setPhone(vo.getPhone());
        user.setGender(0);      // 默认性别：未知
        user.setUserType(1);    // 默认类型：普通用户
        user.setStatus(1);      // 默认状态：正常
        user.setPoints(0);      // 初始积分：0

        // 4. 保存到数据库（ShardingSphere 会自动分库）
        boolean saved = this.save(user);
        
        if (!saved) {
            log.error("用户注册失败: username={}", vo.getUsername());
            throw new BusinessException("注册失败，请稍后重试");
        }

        log.info("用户注册成功: userId={}, username={}", user.getId(), user.getUsername());

        // 5. 缓存用户信息到 Redis
        cacheUserInfo(user);

        // 6. 生成并返回 JWT Token
        return JwtUtil.generateToken(user.getId(), user.getUsername());
    }

    /**
     * 用户登录
     * 
     * @param vo 登录信息
     * @return JWT Token
     */
    @Override
    public String login(LoginVO vo) {
        log.info("用户登录开始: username={}", vo.getUsername());

        // 1. 先尝试从缓存获取用户信息
        String cacheKey = USER_CACHE_PREFIX + "username:" + vo.getUsername();
        User cachedUser = (User) redisTemplate.opsForValue().get(cacheKey);
        
        User user;
        if (cachedUser != null) {
            log.info("从缓存获取用户信息: username={}", vo.getUsername());
            user = cachedUser;
        } else {
            // 2. 缓存未命中，从数据库查询
            LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(User::getUsername, vo.getUsername());
            user = this.getOne(wrapper);
            
            if (user == null) {
                log.warn("用户不存在: {}", vo.getUsername());
                throw new BusinessException("用户名或密码错误");
            }
            
            // 3. 写入缓存
            cacheUserInfo(user);
        }

        // 4. 验证用户状态
        if (user.getStatus() == 0) {
            log.warn("用户已被禁用: username={}", vo.getUsername());
            throw new BusinessException("账号已被禁用，请联系管理员");
        }

        // 5. 验证密码（使用 BCrypt）
        if (!passwordEncoder.matches(vo.getPassword(), user.getPassword())) {
            log.warn("密码错误: username={}", vo.getUsername());
            throw new BusinessException("用户名或密码错误");
        }

        smsService.verifyLoginCode(vo.getUsername(), user.getPhone(), vo.getSmsCode());

        log.info("用户登录成功: userId={}, username={}", user.getId(), user.getUsername());

        // 6. 生成并返回 JWT Token
        return JwtUtil.generateToken(user.getId(), user.getUsername());
    }

    @Override
    public String sendLoginSmsCode(String username) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        User user = this.getOne(wrapper);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException("账号已被禁用");
        }
        return smsService.sendLoginCode(user);
    }

    /**
     * 获取用户信息
     * 
     * @param userId 用户ID
     * @return 用户信息（不包含密码）
     */
    @Override
    public User getUserInfo(Long userId) {
        log.info("获取用户信息: userId={}", userId);

        // 1. 先从缓存获取
        String cacheKey = USER_CACHE_PREFIX + "id:" + userId;
        User cachedUser = (User) redisTemplate.opsForValue().get(cacheKey);
        
        if (cachedUser != null) {
            log.info("从缓存获取用户信息: userId={}", userId);
            // 清除密码字段
            cachedUser.setPassword(null);
            return cachedUser;
        }

        // 2. 缓存未命中，从数据库查询
        User user = this.getById(userId);
        
        if (user == null) {
            log.warn("用户不存在: userId={}", userId);
            throw new BusinessException("用户不存在");
        }

        // 3. 写入缓存
        cacheUserInfo(user);

        // 4. 清除密码字段后返回
        user.setPassword(null);
        return user;
    }

    /**
     * 缓存用户信息到 Redis
     * 
     * @param user 用户对象
     */
    private void cacheUserInfo(User user) {
        try {
            // 按 ID 缓存
            String idKey = USER_CACHE_PREFIX + "id:" + user.getId();
            redisTemplate.opsForValue().set(idKey, user, CACHE_EXPIRE_MINUTES, TimeUnit.MINUTES);
            
            // 按用户名缓存
            String usernameKey = USER_CACHE_PREFIX + "username:" + user.getUsername();
            redisTemplate.opsForValue().set(usernameKey, user, CACHE_EXPIRE_MINUTES, TimeUnit.MINUTES);
            
            log.debug("用户信息已缓存: userId={}, username={}", user.getId(), user.getUsername());
        } catch (Exception e) {
            log.error("缓存用户信息失败: userId={}", user.getId(), e);
            // 缓存失败不影响主流程，只记录日志
        }
    }
}
