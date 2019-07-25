package com.qingcheng.service.goods;
import com.qingcheng.entity.PageResult;
import com.qingcheng.pojo.goods.Sku;
import com.qingcheng.pojo.order.OrderItem;

import java.util.*;

/**
 * sku业务逻辑层
 */
public interface SkuService {


    public List<Sku> findAll();


    public PageResult<Sku> findPage(int page, int size);


    public List<Sku> findList(Map<String,Object> searchMap);


    public PageResult<Sku> findPage(Map<String,Object> searchMap,int page, int size);


    public Sku findById(String id);

    public void add(Sku sku);


    public void update(Sku sku);


    public void delete(String id);

    /**
     * 保存所有的价格到redis
     */
    public void saveAllPriceToRedis();

    /**
     * 根据id查询sku的价格数据
     * @param id
     */
    public Integer findPrice(String id);

    /**
     * 保存价格到redis
     * @param id
     * @param price
     */
    public void savePriceToRedisById(String id,Integer price);

    /**
     * 根据id从缓存中删除价格
     * @param id
     */
    public void deletePriceFromRedis(String id);

    /**
     * 导入索引库
     */
    public void importToEs();

    /**
     * 根据购物车批量删除库存
     * @param orderItems
     * @return
     */
    public boolean deductionStock(List<OrderItem> orderItems);
}
