package com.qingcheng.dao;

import com.qingcheng.pojo.goods.Sku;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import tk.mybatis.mapper.common.Mapper;

public interface SkuMapper extends Mapper<Sku> {

    /**
     * 根据id 扣除库存
     *
     * @param id
     * @param num
     */
    @Update("update tb_sku set num = num- #{num} where id = #{id}")
    public void deductionStock(@Param("id") String id, @Param("num") Integer num);


    /**
     * 根据id 增加销售量
     * @param id
     * @param num
     */
    @Update("update tb_sku set saleNum = saleNum + #{num} where id = #{id}")
    public void addSaleNum(@Param("id") String id, @Param("num") Integer num);

}
