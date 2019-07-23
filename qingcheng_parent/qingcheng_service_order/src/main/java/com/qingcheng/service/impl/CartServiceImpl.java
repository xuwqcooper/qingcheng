package com.qingcheng.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.qingcheng.pojo.goods.Category;
import com.qingcheng.pojo.goods.Sku;
import com.qingcheng.pojo.order.OrderItem;
import com.qingcheng.service.goods.CategoryService;
import com.qingcheng.service.goods.SkuService;
import com.qingcheng.service.order.CartService;
import com.qingcheng.util.CacheKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author: cooper
 * @Description:
 * @Date: Create in 18:18 2019/7/22
 */

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     *从redis中提取购物车
     * @param username
     * @return
     */
    @Override
    public List<Map<String,Object>> findCartList(String username) {
        System.out.println("从redis中提取购物车"+username);
        //从缓存中提取购物车列表
        List<Map<String,Object>> cartList =(List<Map<String,Object>>) redisTemplate.boundHashOps(CacheKey.CART_LIST).get(username);
        if (cartList == null) {//判断缓存中的购物车如果是空
            cartList = new ArrayList<>(); // 将购物车设置为空集合
        }
        return cartList;
    }

    @Reference
    private SkuService skuService;

    @Reference
    private CategoryService categoryService;
    /**
     * 添加商品到购物车
     * @param username
     * @param skuId
     * @param num
     */
    @Override
    public void addItem(String username, String skuId, Integer num) {
        //实现思路： 遍历购物车，如果购物车列表中不存在该商品则添加，存在则累加数量。
        //获取购物车
        List<Map<String, Object>> cartList = findCartList(username);

        boolean flag = false;//是否在购物中存在
        //遍历购物车
        for (Map map : cartList) {
            OrderItem orderItem =(OrderItem) map.get("item");//取出遍历中的一个orderItem
            if (skuId.equals(orderItem.getSkuId())) {//判断添加的商品中是否以存在购物车中 现在是已存在
                if (orderItem.getNum() <= 0) {//如果当前num<0 说明商品有问题
                    cartList.remove(map);//将该商品从商品列表中删除
                    break;
                }
                int weight =  orderItem.getWeight()/ orderItem.getNum();//单个商品重量
                orderItem.setNum(orderItem.getNum() + num);//设置新的num
                if (orderItem.getNum() <= 0) {//因为可能传入的num是个负数, 所以重新判断新设置的
                    cartList.remove(map);
                }
                orderItem.setMoney(orderItem.getPrice()*orderItem.getNum());//设置新的支付金额
                orderItem.setWeight(weight*orderItem.getNum());//设置新的重量
                flag = true;//修改状态
                break;
            }

        }
        if (flag == false) {//购物车中没有此商品
            Sku sku = skuService.findById(skuId);//根据id查询出当前产品
            if (sku == null) {
                throw new RuntimeException("该商品不存在");
            }
            if (!"1".equals(sku.getStatus())) {
                throw new RuntimeException("该商品状态不合法");
            }
            if (num <= 0) {//新增商品数量不能小于等于0
                throw new RuntimeException("商品数量不合法");
            }

            OrderItem orderItem = new OrderItem();

            orderItem.setSkuId(skuId);
            orderItem.setSpuId(sku.getSpuId());
            orderItem.setNum(num);
            orderItem.setPrice(sku.getPrice());
            orderItem.setMoney(sku.getPrice()*num);//设置金额
            orderItem.setImage(sku.getImage());
            orderItem.setName(sku.getName());
            if (sku.getWeight() == null) {
                sku.setWeight(0);
            }
            orderItem.setWeight(sku.getWeight()*num);//重量计算


            //商品分类
            orderItem.setCategoryId3(sku.getCategoryId());//三级id
           Category category3 = (Category) redisTemplate.boundHashOps(CacheKey.CATEGORY).get(sku.getCategoryId());//通过三级id从缓存中查询
            if (category3 == null) {
                category3 = categoryService.findById(sku.getCategoryId());//根据3级分类id查询2级
                redisTemplate.boundHashOps(CacheKey.CATEGORY).put(sku.getCategoryId(), category3);//存入缓存
            }
            orderItem.setCategoryId2(category3.getParentId());

            Category category2 = (Category) redisTemplate.boundHashOps(CacheKey.CATEGORY).get(category3.getParentId());
            if (category2 == null) {
                category2 = categoryService.findById(category3.getParentId());//根据2级分类id查询1级
                redisTemplate.boundHashOps(CacheKey.CATEGORY).put(category3.getParentId(), category2);//存入缓存
            }
            orderItem.setCategoryId1(category2.getParentId());


            Map map = new HashMap();
            map.put("checked", true);//默认选中
            map.put("item", orderItem);
            cartList.add(map);
        }
        redisTemplate.boundHashOps(CacheKey.CART_LIST).put(username, cartList);//重新存入缓存
    }

    /**
     * 更改商品选中状态
     * @param username
     * @param skuId
     * @param checked
     * @return
     */
    @Override
    public boolean updateChecked(String username, String skuId, boolean checked) {
        //获取当前购物车
        List<Map<String, Object>> cartList = findCartList(username);
        boolean isOk = false;//判断商品是否在缓存中存在 默认不存在
        for (Map<String, Object> map : cartList) {
            //取出遍历的每个orderItem
            OrderItem orderItem =(OrderItem) map.get("item");
            //判断更新商品是否在缓存中存在
            if (orderItem.getSkuId().equals(skuId)) {//如果存在
                map.put("checked", checked);//将商品的状态添加到map中
                isOk = true;
                break;
            }
        }
        if (isOk) {//商品在缓存中不存在
            //添加到缓存中 将新的购物车
            redisTemplate.boundHashOps(CacheKey.CART_LIST).put(username, cartList);
        }
        return isOk;
    }

    /**
     * 删除选中的购物车
     * @param username
     */
    @Override
    public void deleteChecked(String username) {
        //获取未选中的购物车 利用stream中的过滤
        List<Map<String, Object>> cartList = findCartList(username).
                stream().filter(cart -> (boolean) cart.get("checked") == false).collect(Collectors.toList());
        //重新添加进缓存 覆盖之前的缓存
        redisTemplate.boundHashOps(CacheKey.CART_LIST).put(username, cartList);
    }
}
