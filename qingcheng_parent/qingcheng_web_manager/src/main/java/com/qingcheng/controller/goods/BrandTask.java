package com.qingcheng.controller.goods;

import com.alibaba.dubbo.config.annotation.Reference;
import com.qingcheng.service.goods.BrandService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @Author: cooper
 * @Description:
 * @Date: Create in 21:01 2019/7/17
 */
@Component
public class BrandTask {

    @Reference
    private BrandService brandService;

    /**
     * 凌晨执行品牌缓存预热
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void saveToRedis() {
        System.out.println("品牌缓存开始预热");
        brandService.saveBrandToRedis();
        System.out.println("品牌缓存预热结束");
    }
}
