package com.qingcheng.pojo.order;

import java.io.Serializable;
import java.util.List;

/**
 * @Author: cooper
 * @Description:
 * @Date: Create in 9:51 2019/7/5
 */
public class OrderDetails implements Serializable {

    private Order order;

    private List<OrderItem> orderItemList;

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public List<OrderItem> getOrderItemList() {
        return orderItemList;
    }

    public void setOrderItemList(List<OrderItem> orderItemList) {
        this.orderItemList = orderItemList;
    }
}
