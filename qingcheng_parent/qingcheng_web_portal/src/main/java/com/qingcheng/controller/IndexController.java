package com.qingcheng.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.qingcheng.pojo.business.Ad;
import com.qingcheng.service.business.AdService;
import com.qingcheng.service.goods.CategoryService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;

/**
 * @Author: cooper
 * @Description:
 * @Date: Create in 19:37 2019/7/12
 */
@Controller
public class IndexController {

    @Reference
    private AdService adService;

    @Reference
    private CategoryService categoryService;

    @RequestMapping("/index")
    public String index(Model model) {
        //查询出轮播图的集合
        List<Ad> lbtList = adService.findByPositon("index_lb");
        //添加到动态渲染
        model.addAttribute("lbt", lbtList);

        //查询categoryTree
        List<Map> categoryList = categoryService.findCategoryTree();
        model.addAttribute("categoryList", categoryList);
        return "index";
    }
}
