package com.qingcheng.controller.goods;

import com.alibaba.dubbo.config.annotation.Reference;
import com.qingcheng.service.goods.SpecService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @Author: cooper
 * @Description:
 * @Date: Create in 21:11 2019/7/17
 */
@Component
public class SpecTask {

    @Reference
    private SpecService specService;

    /**
     * 凌晨开始规格列表缓存预热
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void saveSpecToRedis() {
        System.out.println("开始规格列表缓存预热");
        specService.saveSpecToRedis();
        System.out.println("规格列表缓存预热结束");
    }
}
