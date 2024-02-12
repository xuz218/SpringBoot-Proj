package com.example.backend.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.backend.entity.dto.Account;
import com.example.backend.entity.vo.request.ConfirmResetVO;
import com.example.backend.entity.vo.request.EmailRegisterVO;
import com.example.backend.entity.vo.request.EmailResetVO;
import com.example.backend.mapper.AccountMapper;
import com.example.backend.service.AccountService;
import com.example.backend.utils.Const;
import com.example.backend.utils.FlowUtils;
import jakarta.annotation.Resource;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class AccountServiceImpl extends ServiceImpl<AccountMapper, Account> implements AccountService {

    @Resource
    StringRedisTemplate stringRedisTemplate;

    @Resource
    FlowUtils utils;

    @Resource
    AmqpTemplate amqpTemplate;

    @Resource
    PasswordEncoder encoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Account account = this.findAccountByNameOrEmail(username);
        if (account==null)
            throw new UsernameNotFoundException("用户名或者密码错误");
        return User
                .withUsername(username)
                .password(account.getPassword())
                .roles(account.getRole())
                .build();
    }

    @Override
    public String registerEmailAccount(EmailRegisterVO vo) {
        String email = vo.getEmail();
        String username = vo.getUsername();
        String code = stringRedisTemplate.opsForValue().get(Const.VERIFY_EMAIL_DATA+email);
        if(code==null) return "请先获取验证码";
        if (!code.equals(vo.getCode())) return "验证码错误，请重新输入";
        if (this.existsAccountByEmail(email)) return "此电子邮件已注册";
        if (this.existsAccountByUsername(username)) return "此用户名已被占用，请使用新的用户名";
        String password =encoder.encode(vo.getPassword());
        Account account = new Account(null, username, password, email, "user", new Date());
        if (this.save(account)) {
            stringRedisTemplate.delete(Const.VERIFY_EMAIL_DATA+email);
            return null;
        }else{
            return "内部错误，请联系管理员";
        }
    }

    @Override
    public String resetEmailAccountPassword(EmailResetVO vo) {
        String email = vo.getEmail();
        String verify = this.resetConfirm(new ConfirmResetVO(email, vo.getCode()));
        if (verify!=null) return verify;
        String password = encoder.encode(vo.getPassword());
        boolean update = this.update().eq("email", email).set("password", password).update();
        if (update){
            stringRedisTemplate.delete(Const.VERIFY_EMAIL_DATA+email);
        }
        return null;
    }

    @Override
    public String resetConfirm(ConfirmResetVO vo) {
        String email = vo.getEmail();
        String code = stringRedisTemplate.opsForValue().get(Const.VERIFY_EMAIL_DATA+email);
        if (code==null) return "请先获取验证码";
        if (!code.equals(vo.getCode())) return "验证码错误，请重新输入";
        return null;
    }

    private boolean existsAccountByEmail(String email){
        return this.baseMapper.exists(Wrappers.<Account>query().eq("email",email));
    }

    private boolean existsAccountByUsername(String username){
        return this.baseMapper.exists(Wrappers.<Account>query().eq("username",username));
    }

    public Account findAccountByNameOrEmail(String text) {
        return this.query()
                .eq("username", text).or()
                .eq("email", text)
                .one();
    }


    @Override
    public String registerEmailVerifyCode(String type, String email, String ip) {
        synchronized (ip.intern()){
            if(!this.verifyLimit(ip)){
                return "请求频繁，请稍后再试";
            }
            Random random = new Random();
            int code = random.nextInt(899999)+100000;
            Map<String, Object> data = Map.of("type", type, "email", email, "code", code);
            amqpTemplate.convertAndSend("mail", data);
            stringRedisTemplate.opsForValue()
                    .set(Const.VERIFY_EMAIL_DATA+email, String.valueOf(code), 5, TimeUnit.MINUTES);
            return null;
        }
    }

    private boolean verifyLimit(String ip){
        String key = Const.VERIFY_EMAIL_LIMIT +ip;
        return utils.limitOnceCheck(key,60);
    }
}
