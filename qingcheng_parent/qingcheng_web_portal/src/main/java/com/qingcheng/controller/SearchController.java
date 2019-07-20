package com.qingcheng.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.qingcheng.service.goods.SkuSearchService;
import com.qingcheng.util.WebUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
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

    @GetMapping("/search.do")
    public String search(Model model,@RequestParam Map<String,String> searchMap) throws Exception {//因为是get请求 不能用requestBody
        //字符集处理
        searchMap = WebUtil.convertCharsetToUTF8(searchMap);
        //如果前台传回的当前页码为空 则设置当前页码为第一页
        if (searchMap.get("pageNo") == null || Integer.parseInt(searchMap.get("pageNo"))<1) {//如果前台的页码小于1 也设置为1
            searchMap.put("pageNo", "1");
        }
       /* if (searchMap.get("wantPage") != null) {
            searchMap.put("pageNo", searchMap.get("wantPage"));
        }*/

        //判断前台传回来的sort
        if (searchMap.get("sort") == null) {//如果前台传回来的sort= null 即为 综合那一列的排序
            searchMap.put("sort", "");    //设置为空字符串
        }

        //判断前台传回来的sortOrder的内容 前台和后台的约定
        if (searchMap.get("sortOrder") == null) {//如果传回来的为空
            searchMap.put("sortOrder", "DESC");//默认设置为降序
        }


        //远程调用接口
        Map result = skuSearchService.serch(searchMap);
        model.addAttribute("result", result);
        //url处理
        StringBuffer url = new StringBuffer("/search.do?");
        for (String key : searchMap.keySet()) {
            url.append("&" + key + "=" + searchMap.get(key));
        }
        model.addAttribute("url", url);
        model.addAttribute("searchMap", searchMap);



        Integer pageNo = Integer.parseInt(searchMap.get("pageNo"));//获取当前页页码
        model.addAttribute("pageNo", pageNo);//将当前页码转换为int 类型传回前台

        Long totalPages = (Long) result.get("totalPages");// 总记录数
        int startPage = 1;//设置开始页码
        int endPage = totalPages.intValue();//设置截止位置
        //设置一页显示5页
        if (totalPages > 5) {//如果总页数大于5页
            //首页=当前页减2
            startPage = pageNo - 2;
            if (startPage < 1) {//如果当前页码小于3
                //设置起始页为1
                startPage = 1;
            }
            endPage = startPage + 4;
            //如果当前页码大于总页数-2
            if (endPage > totalPages) {
                //endPage = 总页数
                endPage = totalPages.intValue();
                //起始页 = 总页数-4
                startPage = endPage - 4;
            }
        }

        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);
        return "search";


    }
}
