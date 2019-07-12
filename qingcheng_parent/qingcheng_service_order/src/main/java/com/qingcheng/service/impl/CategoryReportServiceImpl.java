package com.qingcheng.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.qingcheng.dao.CategoryReportMapper;
import com.qingcheng.pojo.order.CategoryReport;
import com.qingcheng.service.order.CategoryReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * @Author: cooper
 * @Description:
 * @Date: Create in 12:22 2019/7/7
 */
@Service(interfaceClass =CategoryReportService.class )
public class CategoryReportServiceImpl implements CategoryReportService {

    @Autowired
    private CategoryReportMapper categoryReportMapper;


    @Override
    public List<CategoryReport> categoryReport(LocalDate localDate) {
        return categoryReportMapper.categoryReport(localDate);
    }

    @Override
    @Transactional// 因为批量操作 所以添加事务
    public void createData() {
        //查询昨天的类目统计数据
        LocalDate localDate = LocalDate.now().minusDays(1);//获取昨天的日期
        List<CategoryReport> categoryReports = categoryReportMapper.categoryReport(localDate);
        for (CategoryReport categoryReport : categoryReports) {
            //保存到tb_category_report表中
            categoryReportMapper.insert(categoryReport);//添加到数据库
        }
    }

    /**
     * 统计一级目录的商品
     * @param date1
     * @param date2
     * @return
     */
    @Override
    public List<Map> category1Count(String date1, String date2) {
        return categoryReportMapper.category1Count(date1, date2);
    }


}
