package com.qingcheng.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.qingcheng.entity.Result;
import com.qingcheng.service.order.CartService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
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
}
