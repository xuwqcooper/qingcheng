package com.qingcheng.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.qingcheng.entity.Result;
import com.qingcheng.pojo.order.Order;
import com.qingcheng.pojo.user.Address;
import com.qingcheng.service.business.AdService;
import com.qingcheng.service.order.CartService;

import com.qingcheng.service.order.OrderService;
import com.qingcheng.service.user.AddressService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;


import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author: cooper
 * @Description:
 * @Date: Create in 18:24 2019/7/22
 */
@RestController
@RequestMapping("/cart")
public class CartConrtroller {

    @Reference
    private CartService cartService;

    /**
     * 从redis中提取购物车列表
     * @return
     */
    @GetMapping("/findCartList.do")
    public List<Map<String,Object>> findCartList() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        List<Map<String,Object>> cartList = cartService.findCartList(username);
        return cartList;
    }


    /**
     * 将商品添加到购物车
     * @param skuId 商品id
     * @param num 商品数量
     * @return
     */
    @GetMapping("/addItem.do")
    public Result addItem(String skuId, Integer num) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        cartService.addItem(username,skuId,num);
        return new Result();
    }

    @RequestMapping("/buy.do")
    public void buy(HttpServletResponse response, String skuId, Integer num) throws IOException {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        cartService.addItem(username,skuId,num);
        response.sendRedirect("/cart.html");

    }

    /**
     * 更新商品的选中状态
     * @return
     */
    @GetMapping("/updateChecked.do")
    public Result updateChecked(String skuId,boolean checked) {
        //获取当前用户名
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        //调用service查询
        cartService.updateChecked(username, skuId, checked);
        return new Result();
    }

    /**
     * 删除选中的购物车
     * @return
     */
    @GetMapping("/deleteCheckedCart.do")
    public Result deleteCheckedCart() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        cartService.deleteChecked(username);
        return new Result();
    }


    /**
     * 计算当前选中购物车的优惠金额
     * @return
     */
    @GetMapping("/preferential.do")
    public Map preferential() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        int preferential = cartService.preferential(username);
        Map map = new HashMap();
        map.put("preferential", preferential);
        return map;
    }

    /**
     * 获取刷新价格后的购物车列表
     * @return
     */
    @GetMapping("/findNewOrderItemList.do")
    public List<Map<String, Object>> findNewOrderItemList() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        List<Map<String, Object>> cartList = cartService.findNewOrderItemList(username);
        return cartList;
    }

    @Reference
    private AddressService addressService;
    /**
     * 查询地址列表
     * @return
     */
    @GetMapping("/findAddressList.do")
    public List<Address> findAddressList() {
        //获取当前用户名
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
         return addressService.findByUsername(username);
    }


    @Reference
    private OrderService orderService;

    /**
     * 保存订单
     * @param order
     * @return
     */
    @PostMapping("/saveOrder.do")
    public Map<String, Object> saveOrder(@RequestBody Order order) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        order.setUsername(username);
        return orderService.add(order);
    }

}
