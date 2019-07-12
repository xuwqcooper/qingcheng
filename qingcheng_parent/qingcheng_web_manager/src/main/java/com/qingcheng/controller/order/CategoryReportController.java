package com.qingcheng.controller.order;

import com.alibaba.dubbo.config.annotation.Reference;
import com.qingcheng.pojo.order.CategoryReport;
import com.qingcheng.service.order.CategoryReportService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * @Author: cooper
 * @Description:
 * @Date: Create in 12:23 2019/7/7
 */
@RestController
@RequestMapping("/categoryReport")
public class CategoryReportController {

    @Reference
    private CategoryReportService categoryReportService;


    /**
     * 昨天的数据统计(商品目录)
     * @return
     */
    @GetMapping("/yesterday.do")
    public List<CategoryReport> yesterday() {
        LocalDate localDate = LocalDate.now().minusDays(1);//获取到昨天的时间
        return categoryReportService.categoryReport(localDate);
    }

    @GetMapping("/category1Count.do")
    public List<Map> category1Count(String date1, String date2) {
            return categoryReportService.category1Count(date1, date2);
    }
}
