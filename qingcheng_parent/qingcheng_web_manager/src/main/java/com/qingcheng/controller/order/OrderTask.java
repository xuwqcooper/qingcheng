package com.qingcheng.controller.order;

import com.alibaba.dubbo.config.annotation.Reference;
import com.qingcheng.service.order.CategoryReportService;
import com.qingcheng.service.order.OrderService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;

import java.util.Date;


/**
 * @Author: cooper
 * @Description:
 * @Date: Create in 20:45 2019/7/5
 */
@Controller
public class OrderTask {

    @Reference
    private OrderService orderService;

    @Reference
    private CategoryReportService categoryReportService;


    @Scheduled(cron = "* 0/2 * * * ?")
    public void orderTimeOutLogic() {
        System.out.println("每2分钟间隔执行一次任务"+new Date());
            orderService.orderTimeOutLogic();
    }

    /**
     * 定时添加数据
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void createData() {
        System.out.println("每天凌晨1点执行一次");
        categoryReportService.createData();
    }

}
