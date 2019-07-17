package com.qingcheng.service.impl;

import com.qingcheng.service.business.AdService;
import com.qingcheng.service.goods.BrandService;
import com.qingcheng.service.goods.CategoryService;
import com.qingcheng.service.goods.SkuService;
import com.qingcheng.service.goods.SpecService;
import com.qingcheng.util.CacheKey;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
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

    @Autowired
    private BrandService brandService;

    @Autowired
    private SpecService specService;

    @Autowired
    private RedisTemplate redisTemplate;

    public void afterPropertiesSet() throws Exception {
        System.out.println("缓存预热");
        categoryService.saveCategoryTreeToRedis();//加载商品分类导航缓存
        //加载所有的sku的价格
        skuService.saveAllPriceToRedis();

        //加载索引库数据
        skuService.importToEs();

        //如果缓存中不包含品牌列表的缓存分类
        if (!redisTemplate.hasKey(CacheKey.BRAND_LIST)) {
            //将品牌添加到缓存分类
            brandService.saveBrandToRedis();
        }
        //如果缓存中不包含规格列表的缓存分类
        if (!redisTemplate.hasKey(CacheKey.SPEC_LIST)) {
            //将规格列表添加到缓存分类
            specService.saveSpecToRedis();
        }


    }
}
