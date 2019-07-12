package com.qingcheng.service.order;

import com.qingcheng.pojo.order.CategoryReport;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * @Author: cooper
 * @Description:
 * @Date: Create in 12:18 2019/7/7
 */
public interface CategoryReportService {

    public List<CategoryReport> categoryReport(LocalDate localDate);

    public void createData();

    public List<Map> category1Count(String date1, String date2);
}
