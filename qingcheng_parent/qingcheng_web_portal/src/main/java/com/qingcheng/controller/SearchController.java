package com.qingcheng.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.qingcheng.service.goods.SkuSearchService;
import com.qingcheng.util.WebUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * @Author: cooper
 * @Description:
 * @Date: Create in 19:18 2019/7/16
 */
@Controller
public class SearchController {

    @Reference
    private SkuSearchService skuSearchService;

    @RequestMapping("/search.do")
    public String search(Model model,@RequestParam Map<String,String> searchMap) throws Exception {//因为是get请求 不能用requestBody
        //字符集处理
        searchMap = WebUtil.convertCharsetToUTF8(searchMap);
        //远程调用接口
        Map result = skuSearchService.serch(searchMap);
        model.addAttribute("result", result);
        StringBuffer url = new StringBuffer("/search.do");
        for (String key : searchMap.keySet()) {
            url.append("&" + key + "=" + searchMap.get(key));
        }
        model.addAttribute("url", url);
        return "search";


    }
}
