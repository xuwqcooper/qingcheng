package com.qingcheng.service.order;
import com.qingcheng.entity.PageResult;
import com.qingcheng.pojo.order.IdAndNum;
import com.qingcheng.pojo.order.Order;
import com.qingcheng.pojo.order.OrderDetails;
import com.sun.org.apache.xpath.internal.operations.Or;

import java.util.*;

/**
 * order业务逻辑层
 */
public interface OrderService {


    public List<Order> findAll();


    public PageResult<Order> findPage(int page, int size);


    public List<Order> findList(Map<String,Object> searchMap);


    public PageResult<Order> findPage(Map<String,Object> searchMap,int page, int size);


    public Order findById(String id);

    public Map<String,Object> add(Order order);


    public void update(Order order);


    public void delete(String id);

    public OrderDetails findOrderDetailsById(String id);

    public List<Order> findByIds(Map<String,Object> searchMap);

    public void batchSend(List<Order> orderList);

    /**
     * 订单超时逻辑处理
     */
    public void orderTimeOutLogic();

    public void merge(String orderId1, String orderId2);

    public void split( IdAndNum[] searchMap);


}
