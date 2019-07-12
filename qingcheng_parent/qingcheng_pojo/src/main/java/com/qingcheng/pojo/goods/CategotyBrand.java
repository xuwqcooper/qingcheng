package com.qingcheng.pojo.goods;

import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @Author: cooper
 * @Description:
 * @Date: Create in 15:15 2019/7/4
 */
@Table(name = "tb_category_brand")
public class CategotyBrand {

    @Id
    private Integer categoryId;

    @Id
    private Integer brandId;

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public Integer getBrandId() {
        return brandId;
    }

    public void setBrandId(Integer brandId) {
        this.brandId = brandId;
    }
}
