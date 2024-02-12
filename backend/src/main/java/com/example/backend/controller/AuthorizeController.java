package com.example.backend.controller;

import com.example.backend.entity.RestBean;
import com.example.backend.entity.vo.request.ConfirmResetVO;
import com.example.backend.entity.vo.request.EmailRegisterVO;
import com.example.backend.entity.vo.request.EmailResetVO;
import com.example.backend.service.AccountService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.function.Function;
import java.util.function.Supplier;

@Validated
@RestController
@RequestMapping("/api/auth")
public class AuthorizeController {
    @Resource
    AccountService service;

    @GetMapping("/ask-code")
    public RestBean<Void> askVerifyCode(@RequestParam @Email String email,
                                        @RequestParam @Pattern(regexp = "(register|reset)") String type,
                                        HttpServletRequest request){
        String message = service.registerEmailVerifyCode(type, email, request.getRemoteAddr());
        return this.messgaeHandle(()->service.registerEmailVerifyCode(type, email, request.getRemoteAddr()));
    }

    @PostMapping("/register")
    public RestBean<Void> register(@RequestBody @Valid EmailRegisterVO vo){
        return this.messgaeHandle(()->service.registerEmailAccount(vo));
    }

    @PostMapping("/reset-confirm")
    public RestBean<Void> resetConfirm(@RequestBody @Valid ConfirmResetVO vo){
        return this.messageHandle(vo, service::resetConfirm);
    }

    @PostMapping("/reset-password")
    public RestBean<Void> resetConfirm(@RequestBody @Valid EmailResetVO vo){
        return this.messageHandle(vo, service::resetEmailAccountPassword);
    }

    private <T> RestBean<Void> messageHandle(T vo, Function<T, String> function){
        return messgaeHandle(()-> function.apply(vo));
    }

    private RestBean<Void> messgaeHandle(Supplier<String> action){
        String message = action.get();
        return message==null ? RestBean.success(): RestBean.failure(400, message);
    }
}
