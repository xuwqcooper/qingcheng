package com.qingcheng.controller.order;

import com.alibaba.dubbo.config.annotation.Reference;
import com.qingcheng.entity.PageResult;
import com.qingcheng.entity.Result;
import com.qingcheng.pojo.order.IdAndNum;
import com.qingcheng.pojo.order.Order;
import com.qingcheng.pojo.order.OrderDetails;
import com.qingcheng.service.order.OrderService;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/order")
public class OrderController {

    @Reference
    private OrderService orderService;

    @GetMapping("/findAll")
    public List<Order> findAll(){
        return orderService.findAll();
    }

    @GetMapping("/findPage")
    public PageResult<Order> findPage(int page, int size){
        return orderService.findPage(page, size);
    }

    @PostMapping("/findList")
    public List<Order> findList(@RequestBody Map<String,Object> searchMap){
        return orderService.findList(searchMap);
    }

    @PostMapping("/findPage")
    public PageResult<Order> findPage(@RequestBody Map<String,Object> searchMap,int page, int size){
        return  orderService.findPage(searchMap,page,size);
    }

    @GetMapping("/findById")
    public Order findById(String id){
        return orderService.findById(id);
    }


    @PostMapping("/add")
    public Result add(@RequestBody Order order){
        orderService.add(order);
        return new Result();
    }

    @PostMapping("/update")
    public Result update(@RequestBody Order order){
        orderService.update(order);
        return new Result();
    }

    @GetMapping("/delete")
    public Result delete(String id){
        orderService.delete(id);
        return new Result();
    }

    @GetMapping("/findOrderDetailsById.do")
    public OrderDetails findOrderDetailsById(String id) {
        return orderService.findOrderDetailsById(id);
    }

    @PostMapping("/findByIds.do")
    public List<Order> findByIds(@RequestBody Map<String,Object> searchMap) {
        return orderService.findByIds(searchMap);
    }


    @PostMapping("/batchSend.do")
    public Result batchSend(List<Order> orders) {
        orderService.batchSend(orders);
        return new Result();
    }

    @GetMapping("/merge.do")
    public Result merge(String orderId1, String orderId2) {
        orderService.merge(orderId1,orderId2);
        return new Result();
    }

    @PostMapping("/split.do")
    public void split(@RequestBody IdAndNum[] searchMap) {
        orderService.split(searchMap);
    }
}
