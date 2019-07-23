package com.qingcheng.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author: cooper
 * @Description:
 * @Date: Create in 21:56 2019/7/21
 */
@RestController
@RequestMapping("/login")
public class LoginController {

    /**
     * 获取用户名
     * @return
     */
    @GetMapping("/username.do")
    public Map username() {
        //获取当前用户登录名
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println("当前登录用户名:" + username);
        if ("anonymousUser".equals(username)) {//未登录
            username = "";
        }
        Map map = new HashMap();
        map.put("username", username);
        return map;


    }
}
