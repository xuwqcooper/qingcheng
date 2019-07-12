package com.qingcheng.dao;

import com.qingcheng.pojo.order.CategoryReport;
import com.sun.javafx.logging.PulseLogger;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * @Author: cooper
 * @Description:
 * @Date: Create in 11:27 2019/7/7
 */
public interface CategoryReportMapper extends Mapper<CategoryReport> {

    @Select("SELECT oi.`category_id1` categoryId1,oi.`category_id2` categoryId2,oi.`category_id3` categoryId3, DATE_FORMAT(o.`pay_time`,'%Y-%m-%d') countDate,SUM(oi.`num`) num ,SUM(oi.`pay_money`) money " +
            "FROM tb_order o,tb_order_item oi " +
            "WHERE o.`id`=oi.`order_id` AND o.`pay_status`='1' AND o.`is_delete`='0' AND DATE_FORMAT(o.`pay_time`,'%Y-%m-%d')=#{date} " +
            "GROUP BY oi.`category_id1`,oi.`category_id2`,oi.`category_id3`,DATE_FORMAT(o.`pay_time`,'%Y-%m-%d');")
    public List<CategoryReport> categoryReport(@Param("date") LocalDate date);


    @Select("SELECT r.`category_id1` categoryId1,c.`name` categoryName,SUM(r.num) num,SUM(r.`money`) money " +
            "FROM tb_category_report r,v_category1 c " +
            "WHERE r.`count_date`>=#{date1} AND r.`count_date`<=#{date2} AND r.`category_id1`=c.`id` " +
            "GROUP BY r.`category_id1`,c.`name` ")
    public List<Map> category1Count(@Param("date1") String date1, @Param("date2") String date2);

}
