package com.qingcheng.service.impl;

import com.qingcheng.service.business.AdService;
import com.qingcheng.service.goods.CategoryService;
import com.qingcheng.service.goods.SkuService;
import com.qingcheng.util.CacheKey;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @Author: cooper
 * @Description:
 * @Date: Create in 16:14 2019/7/13
 */
@Component
public class Init implements InitializingBean {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private SkuService skuService;

    public void afterPropertiesSet() throws Exception {
        System.out.println("缓存预热");
        categoryService.saveCategoryTreeToRedis();//加载商品分类导航缓存
        //加载所有的sku的价格
        skuService.saveAllPriceToRedis();

        //加载索引库数据
        skuService.importToEs();
    }
}
