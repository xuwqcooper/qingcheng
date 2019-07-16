package com.qingcheng.service.impl;

import com.qingcheng.service.business.AdService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @Author: cooper
 * @Description:
 * @Date: Create in 22:17 2019/7/13
 */
@Component
public class Init implements InitializingBean {

    @Autowired
    private AdService adService;

    public void afterPropertiesSet() throws Exception {
        System.out.println("缓存预热");
        adService.sendAllAdToRedis();
    }
}
