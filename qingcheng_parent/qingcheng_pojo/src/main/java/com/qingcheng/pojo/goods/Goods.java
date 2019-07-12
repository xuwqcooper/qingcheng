package com.qingcheng.pojo.goods;

import java.io.Serializable;
import java.util.List;

/**
 * @Author: cooper
 * @Description:
 * @Date: Create in 12:02 2019/7/4
 */
public class Goods implements Serializable {

    private Spu spu;

    private List<Sku> skuList;

    public Spu getSpu() {
        return spu;
    }

    public void setSpu(Spu spu) {
        this.spu = spu;
    }

    public List<Sku> getSkuList() {
        return skuList;
    }

    public void setSkuList(List<Sku> skuList) {
        this.skuList = skuList;
    }
}
