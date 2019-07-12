package com.qingcheng.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.qingcheng.pojo.system.LoginLog;
import com.qingcheng.service.system.LoginLogService;
import com.qingcheng.util.WebUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;

/**
 * @Author: cooper
 * @Description:
 * @Date: Create in 0:33 2019/7/10
 */
public class AuthenticationSuccessHandlerImpl implements AuthenticationSuccessHandler {

    @Reference
    private LoginLogService loginLogService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Authentication authentication) throws IOException, ServletException {
        String name = authentication.getName();
        String ip = httpServletRequest.getRemoteAddr();
        String agent = httpServletRequest.getHeader("user-agent");
        LoginLog loginLog = new LoginLog();
        loginLog.setLoginName(name);//当前登录用户
        loginLog.setLoginTime(new Date());//当前登录时间
        loginLog.setIp(ip);//获取访问的ip
        loginLog.setLocation(WebUtil.getCityByIP(ip));//登录的城市
        loginLog.setBrowserName(WebUtil.getBrowserName(agent));//登录的浏览器
        loginLogService.add(loginLog);

        httpServletRequest.getRequestDispatcher("/main.html").forward(httpServletRequest,httpServletResponse);
    }
}
