package com.qingcheng.service.order;

import java.util.List;
import java.util.Map;

/**
 * @Author: cooper
 * @Description:
 * @Date: Create in 18:16 2019/7/22
 */
public interface CartService {

    /**
     * 购物车服务 从redis中提取购物车
     * @param username
     * @return
     */
    public List<Map<String,Object>> findCartList(String username);

    /**
     * 添加商品到购物车
     * @param username
     * @param skuId
     * @param num
     */
    public void addItem(String username, String skuId, Integer num);

    /**
     * 更改商品选中状态
     * @param username
     * @param skuId
     * @param checked
     * @return
     */
    public boolean updateChecked(String username,String skuId,boolean checked);

    /**
     * 删除选中的购物车
     * @param username
     */
    public void deleteChecked(String username);

    /**
     * 计算购物车中当前选中的优惠金额
     * @param username
     * @return
     */
    public int preferential(String username);
}
