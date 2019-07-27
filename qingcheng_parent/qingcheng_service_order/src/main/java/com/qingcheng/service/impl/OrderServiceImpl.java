package com.qingcheng.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.qingcheng.dao.OrderConfigMapper;
import com.qingcheng.dao.OrderItemMapper;
import com.qingcheng.dao.OrderLogMapper;
import com.qingcheng.dao.OrderMapper;
import com.qingcheng.entity.PageResult;
import com.qingcheng.pojo.order.*;
import com.qingcheng.service.goods.SkuService;
import com.qingcheng.service.order.CartService;
import com.qingcheng.service.order.OrderService;
import com.qingcheng.util.IdWorker;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service(interfaceClass = OrderService.class)
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private OrderLogMapper orderLogMapper;

    @Autowired
    private OrderConfigMapper orderConfigMapper;

    @Autowired
    private IdWorker idWorker;

    /**
     * 返回全部记录
     *
     * @return
     */
    public List<Order> findAll() {
        return orderMapper.selectAll();
    }

    /**
     * 分页查询
     *
     * @param page 页码
     * @param size 每页记录数
     * @return 分页结果
     */
    public PageResult<Order> findPage(int page, int size) {
        PageHelper.startPage(page, size);
        Page<Order> orders = (Page<Order>) orderMapper.selectAll();
        return new PageResult<Order>(orders.getTotal(), orders.getResult());
    }

    /**
     * 条件查询
     *
     * @param searchMap 查询条件
     * @return
     */
    public List<Order> findList(Map<String, Object> searchMap) {
        Example example = createExample(searchMap);
        return orderMapper.selectByExample(example);
    }

    /**
     * 分页+条件查询
     *
     * @param searchMap
     * @param page
     * @param size
     * @return
     */
    public PageResult<Order> findPage(Map<String, Object> searchMap, int page, int size) {
        PageHelper.startPage(page, size);
        Example example = createExample(searchMap);
        Page<Order> orders = (Page<Order>) orderMapper.selectByExample(example);
        return new PageResult<Order>(orders.getTotal(), orders.getResult());
    }

    /**
     * 根据Id查询
     *
     * @param id
     * @return
     */
    public Order findById(String id) {
        return orderMapper.selectByPrimaryKey(id);
    }

    @Autowired
    private CartService cartService;

    @Reference
    private SkuService skuService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 新增
     *
     * @param order
     */
    @Transactional
    public Map<String, Object> add(Order order) {
        //获取购物车  刷新单价的购物车
        List<Map<String, Object>> cartList = cartService.findNewOrderItemList(order.getUsername());
        //获取选中的购物车
        List<OrderItem> orderItems = cartList.stream().filter(cart -> (boolean) cart.get("checked"))
                .map(cart -> (OrderItem) cart.get("item"))
                .collect(Collectors.toList());
        //扣减库存
        if (!skuService.deductionStock(orderItems)) {
            throw new RuntimeException("库存不足");
        }
        //保存订单主表
        order.setId(idWorker.nextId() + "");
        try {
            //合计数计算
            IntStream numStream = orderItems.stream().mapToInt(OrderItem::getNum);
            IntStream moneyStream = orderItems.stream().mapToInt(OrderItem::getMoney);
            int totalNum = numStream.sum();//总数量
            int totalMoney = moneyStream.sum();//总金额
            int preMoney = cartService.preferential(order.getUsername());//优惠金额


            order.setPreMoney(preMoney);//优惠金额
            order.setTotalMoney(totalMoney);//总金额
            order.setTotalNum(totalNum);//总数量
            order.setPayMoney(totalMoney - preMoney);//支付金额= 总金额- 优惠金额
            order.setCreateTime(new Date());//订单创建时间
            order.setOrderStatus("0");//订单状态
            order.setPayStatus("0");//支付状态:未支付
            order.setConsignStatus("0");//发货状态 :未发货
            orderMapper.insert(order);//添加到数据库
            //保存订单明细
            //打折比例
            double proportion = (double)order.getPayMoney() / totalMoney;
            for (OrderItem orderItem : orderItems) {
                orderItem.setOrderId(order.getId());//订单主表id
                orderItem.setId(idWorker.nextId() + "");//id 分布式id
                orderItem.setPayMoney((int) (orderItem.getMoney() * proportion));//支付金额
                orderItemMapper.insert(orderItem);//添加到数据库
            }
        } catch (Exception e) {
            rabbitTemplate.convertAndSend("","queue.skuback", JSON.toJSONString(orderItems));
            throw new RuntimeException("订单生成失败");//抛出异常,让其回滚!
        }
        //清除选中购物车
        cartService.deleteChecked(order.getUsername());
        //返回订单号和支付金额
        Map<String, Object> map = new HashMap();
        map.put("ordersn", order.getId());//订单号
        map.put("money", order.getPayMoney());//支付金额

        return map;
    }
    /**
     * 修改
     * @param order
     */
    public void update(Order order) {
        orderMapper.updateByPrimaryKeySelective(order);
    }

    /**
     *  删除
     * @param id
     */
    public void delete(String id) {
        orderMapper.deleteByPrimaryKey(id);
    }

    /**
     * 根据id查询订单对象和订单明细列表的组合
     * @param id
     * @return
     */
    @Transactional
    public OrderDetails findOrderDetailsById(String id) {

        //查询订单对象 order
        Order order = orderMapper.selectByPrimaryKey(id);
        //查询订单明细列表
        Example example = new Example(OrderItem.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("orderId", id);
        List<OrderItem> orderItemList = orderItemMapper.selectByExample(example);

        //封装orderDetails
        OrderDetails orderDetails = new OrderDetails();
        orderDetails.setOrder(order);
        orderDetails.setOrderItemList(orderItemList);
        return orderDetails;
    }

    /**
     * 返回待发货订单
     * @param searchMap
     * @return
     */
    public List<Order> findByIds(Map<String,Object> searchMap) {

        Example example = new Example(Order.class);
        Example.Criteria criteria = example.createCriteria();
        if (searchMap.get("ids") != null) {
            criteria.andIn("id", Arrays.asList((String[])searchMap.get("ids")));
        }
        criteria.andEqualTo("consignStatus", "0");//发货状态 :未发货
        criteria.andEqualTo("payStatus", "1");//支付状态 已支付
        criteria.andEqualTo("orderStatus", "1");//订单状态 待发货
        criteria.andEqualTo("isDelete", "0");// 是否删除 否
        List<Order> orderList = orderMapper.selectByExample(example);

        return orderList;
    }

    /**
     * 批量发货
     * @param orderList
     */
    @Transactional
    public void batchSend(List<Order> orderList) {
        //判断订单是否添加配送方式或物流单号
        for (Order order : orderList) {
            if (order.getShippingCode() == null || order.getShippingName() == null) {
                throw new RuntimeException("尚未添加配送方式或物流单号");
            }
        }

        //修改状态
        for (Order order : orderList) {
            order.setOrderStatus("2");//订单状态 已发货
            order.setConsignStatus("1");//发货状态 已发货
            order.setCloseTime(new Date());//创建发货时间
            orderMapper.updateByPrimaryKeySelective(order);
            //记录订单日志
            OrderLog orderLog = new OrderLog();
            orderLog.setConsignStatus(order.getConsignStatus());//设置发货状态
            orderLog.setPayStatus(order.getPayStatus());//设置交易状态
            orderLog.setOrderStatus(order.getOrderStatus());//设置订单状态
            orderLog.setOperateTime(order.getCreateTime());//设置操作时间

        }
    }

    /**
     * 订单超时处理
     */
    @Transactional
    public void orderTimeOutLogic() {

        //订单未付款 自动关闭
        //查询超时时间
        OrderConfig orderConfig = orderConfigMapper.selectByPrimaryKey(1);
        Integer orderTimeout = orderConfig.getOrderTimeout();
        LocalDateTime localDateTime = LocalDateTime.now().minusMinutes(orderTimeout);
        //设置查询条件
        Example example = new Example(Order.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andLessThan("createTime", localDateTime);//创建时间小于超时时间的
        criteria.andEqualTo("orderStatus", "0");//订单状态为未付款的
        criteria.andEqualTo("isDelete", "0");//订单未被删除的
        //查询超时订单
        List<Order> orders = orderMapper.selectByExample(example);
        for (Order order : orders) {
            //记录订单变动日志
            OrderLog orderLog = new OrderLog();
            orderLog.setOperater("system");//系统
            orderLog.setOrderStatus("4");
            orderLog.setOperateTime(new Date());//当前日期
            orderLog.setPayStatus(order.getPayStatus());
            orderLog.setConsignStatus(order.getConsignStatus());
            orderLog.setRemarks("订单超时,系统自动关闭");
            orderLog.setId(order.getId());
            orderLogMapper.insert(orderLog);
            //更改订单状态
            order.setOrderStatus("4");
            order.setCloseTime(new Date());//当前时间
            orderMapper.updateByPrimaryKeySelective(order);//更新
        }
    }

    /**
     *合并订单
     * @param orderId1
     * @param orderId2
     * @return
     */
    @Transactional
    public void merge(String orderId1, String orderId2) {

        Order order1 = orderMapper.selectByPrimaryKey(orderId1);
        Order order2 = orderMapper.selectByPrimaryKey(orderId2);
        if (order1 == null || order2 == null) {
            throw new RuntimeException("合并订单失败,有订单不存在");
        }
        order1.setTotalNum(order1.getTotalNum()+order2.getTotalNum());//商品总数量
        order1.setTotalMoney(order1.getTotalMoney()+order2.getTotalMoney());//商品总金额
        order1.setPayMoney(order1.getPreMoney()+order2.getPayMoney());//商品总实际支付金额
        order1.setPreMoney(order1.getPreMoney()+order2.getPreMoney());//商品总优惠金额
        order1.setUpdateTime(new Date());//更新时间
        orderMapper.updateByPrimaryKeySelective(order1);//更新主订单 order1
        //查询order2的订单详情
        Example example = new Example(OrderItem.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("orderId", orderId2);
        List<OrderItem> orderItemList = orderItemMapper.selectByExample(example);
        for (OrderItem orderItem : orderItemList) {
            //将orderid设置为order1的id
            orderItem.setOrderId(orderId1);
            orderItemMapper.updateByPrimaryKeySelective(orderItem);//更新
        }
        //将订单2添加逻辑删除
        order2.setIsDelete("1");
        orderMapper.updateByPrimaryKeySelective(order2);
        //记录订单日志
        OrderLog orderLog = new OrderLog();
        orderLog.setOperateTime(new Date());
        orderLog.setOrderId(orderId1);
        orderLog.setRemarks("合并"+orderId1+" "+orderId2+"两个订单");


    }

    /**
     * 拆分订单
     * @param searchMap
     */
    @Transactional
    public void split(IdAndNum[] searchMap) {
        if (searchMap != null) {
            String orderItemId1 = searchMap[0].getOrderItemId();
            OrderItem orderItem1 = orderItemMapper.selectByPrimaryKey(orderItemId1);
            //获取原本的order
            Order order = orderMapper.selectByPrimaryKey(orderItem1.getOrderId());
            //将设置order赋值给新创建的一个order1
            Order order1 = new Order();
            BeanUtils.copyProperties(order1,order);
            //设置id
            String orderId1 = idWorker.nextId() + "";
            order1.setId(orderId1);
            orderMapper.insert(order1);
            //设置初始化值
            order1.setTotalNum(0);
            order1.setPayMoney(0);
            order1.setTotalMoney(0);
            orderMapper.updateByPrimaryKeySelective(order1);//更新
            for (IdAndNum idAndNum : searchMap) {
                //获得一个orderItemId
                String orderItemId = idAndNum.getOrderItemId();
                //获得一个订单详情
                OrderItem orderItem2 = orderItemMapper.selectByPrimaryKey(orderItemId);
                OrderItem orderItem = new OrderItem();
                BeanUtils.copyProperties(orderItem2,orderItem);

                //设置orderItem的orderId
                orderItem.setOrderId(orderId1);
                orderItemMapper.insert(orderItem);//添加到数据库
                //获取拆分数量
                Integer num = idAndNum.getNum();
                //判断拆分的数量与原总数量的关系
                if (orderItem.getNum() < num) {
                    throw new RuntimeException("数据有误,请重新填写拆分数量");
                }
                orderItem.setMoney(num*orderItem2.getPrice());//设置总金额
                orderItem.setNum(num);//设置数量
                orderItem.setPayMoney(orderItem.getPayMoney()/orderItem.getNum()*num);//设置应付金额
                orderItemMapper.updateByPrimaryKeySelective(orderItem);//更新

                //设置原本的orderItem2的属性
                orderItem2.setNum(orderItem2.getNum()-num);//设置数量
                orderItem2.setMoney(orderItem2.getMoney()-orderItem.getMoney());//设置总金额
                orderItem2.setPayMoney(orderItem2.getPayMoney() - orderItem.getPayMoney());//设置支付金额
                orderItemMapper.updateByPrimaryKeySelective(orderItem2);//更新

                //设置拆分订单的数量
                order1.setTotalNum(order1.getTotalNum()+orderItem.getNum());
                //设置拆分订单的总金额
                order1.setTotalMoney(order1.getTotalMoney()+num*orderItem.getPrice());
                //设置拆分订单的实际支付金额
                order1.setPayMoney(order1.getPayMoney()+orderItem.getPayMoney()*num);
                orderMapper.updateByPrimaryKeySelective(order1);//更新

            }
            //设置拆分订单的优惠金额=总金额-实际支付金额
            order1.setPreMoney(order1.getTotalMoney()-order1.getPayMoney());
            orderMapper.updateByPrimaryKeySelective(order1);//更新
            //设置拆分后主订单的属性
            order.setTotalNum(order.getTotalNum()-order1.getTotalNum());//设置拆分后的订单数量
            order.setTotalMoney(order.getTotalMoney()-order1.getTotalMoney());//设置拆分后的总金额
            order.setPayMoney(order.getPayMoney()-order1.getPayMoney());//设置拆分后的支付金额
            order.setPreMoney(order.getPreMoney()-order1.getPreMoney());//设置拆分后的优惠金额
            orderMapper.updateByPrimaryKeySelective(order);//更新
        }

    }


    /**
     * 修改订单状态
     * @param orderId
     * @param transactionId
     */
    @Override
    public void updatePayStatus(String orderId, String transactionId) {
        Order order = orderMapper.selectByPrimaryKey(orderId);
        if (order != null && "0".equals(order.getPayStatus())) {
            //修改订单状态等信息
            order.setOrderStatus("1");//订单状态
            order.setPayStatus("1");//支付状态
            order.setUpdateTime(new Date());//修改日期
            order.setPayTime(new Date());//支付日期
            order.setTransactionId(transactionId);//交易流水号
            orderMapper.updateByPrimaryKeySelective(order);//修改
            //记录订单日志
            OrderLog orderLog = new OrderLog();
            orderLog.setId(idWorker.nextId()+"");//id
            orderLog.setOperateTime(new Date());//操作时间
            orderLog.setOperater("system");//系统操作
            orderLog.setOrderStatus("1");//订单状态
            orderLog.setPayStatus("1");//支付状态
            orderLog.setRemarks("支付流水号:"+transactionId);//备注
            orderLog.setOrderId(orderId);
            orderLogMapper.insert(orderLog);

        }
    }

    /**
     * 构建查询条件
     * @param searchMap
     * @return
     */
    private Example createExample(Map<String, Object> searchMap){
        Example example=new Example(Order.class);
        Example.Criteria criteria = example.createCriteria();
        if(searchMap!=null){
            // 订单id
            if(searchMap.get("id")!=null && !"".equals(searchMap.get("id"))){
                criteria.andLike("id","%"+searchMap.get("id")+"%");
            }
            // 支付类型，1、在线支付、0 货到付款
            if(searchMap.get("payType")!=null && !"".equals(searchMap.get("payType"))){
                criteria.andLike("payType","%"+searchMap.get("payType")+"%");
            }
            // 物流名称
            if(searchMap.get("shippingName")!=null && !"".equals(searchMap.get("shippingName"))){
                criteria.andLike("shippingName","%"+searchMap.get("shippingName")+"%");
            }
            // 物流单号
            if(searchMap.get("shippingCode")!=null && !"".equals(searchMap.get("shippingCode"))){
                criteria.andLike("shippingCode","%"+searchMap.get("shippingCode")+"%");
            }
            // 用户名称
            if(searchMap.get("username")!=null && !"".equals(searchMap.get("username"))){
                criteria.andLike("username","%"+searchMap.get("username")+"%");
            }
            // 买家留言
            if(searchMap.get("buyerMessage")!=null && !"".equals(searchMap.get("buyerMessage"))){
                criteria.andLike("buyerMessage","%"+searchMap.get("buyerMessage")+"%");
            }
            // 是否评价
            if(searchMap.get("buyerRate")!=null && !"".equals(searchMap.get("buyerRate"))){
                criteria.andLike("buyerRate","%"+searchMap.get("buyerRate")+"%");
            }
            // 收货人
            if(searchMap.get("receiverContact")!=null && !"".equals(searchMap.get("receiverContact"))){
                criteria.andLike("receiverContact","%"+searchMap.get("receiverContact")+"%");
            }
            // 收货人手机
            if(searchMap.get("receiverMobile")!=null && !"".equals(searchMap.get("receiverMobile"))){
                criteria.andLike("receiverMobile","%"+searchMap.get("receiverMobile")+"%");
            }
            // 收货人地址
            if(searchMap.get("receiverAddress")!=null && !"".equals(searchMap.get("receiverAddress"))){
                criteria.andLike("receiverAddress","%"+searchMap.get("receiverAddress")+"%");
            }
            // 订单来源：1:web，2：app，3：微信公众号，4：微信小程序  5 H5手机页面
            if(searchMap.get("sourceType")!=null && !"".equals(searchMap.get("sourceType"))){
                criteria.andLike("sourceType","%"+searchMap.get("sourceType")+"%");
            }
            // 交易流水号
            if(searchMap.get("transactionId")!=null && !"".equals(searchMap.get("transactionId"))){
                criteria.andLike("transactionId","%"+searchMap.get("transactionId")+"%");
            }
            // 订单状态
            if(searchMap.get("orderStatus")!=null && !"".equals(searchMap.get("orderStatus"))){
                criteria.andLike("orderStatus","%"+searchMap.get("orderStatus")+"%");
            }
            // 支付状态
            if(searchMap.get("payStatus")!=null && !"".equals(searchMap.get("payStatus"))){
                criteria.andLike("payStatus","%"+searchMap.get("payStatus")+"%");
            }
            // 发货状态
            if(searchMap.get("consignStatus")!=null && !"".equals(searchMap.get("consignStatus"))){
                criteria.andLike("consignStatus","%"+searchMap.get("consignStatus")+"%");
            }
            // 是否删除
            if(searchMap.get("isDelete")!=null && !"".equals(searchMap.get("isDelete"))){
                criteria.andLike("isDelete","%"+searchMap.get("isDelete")+"%");
            }

            // 数量合计
            if(searchMap.get("totalNum")!=null ){
                criteria.andEqualTo("totalNum",searchMap.get("totalNum"));
            }
            // 金额合计
            if(searchMap.get("totalMoney")!=null ){
                criteria.andEqualTo("totalMoney",searchMap.get("totalMoney"));
            }
            // 优惠金额
            if(searchMap.get("preMoney")!=null ){
                criteria.andEqualTo("preMoney",searchMap.get("preMoney"));
            }
            // 邮费
            if(searchMap.get("postFee")!=null ){
                criteria.andEqualTo("postFee",searchMap.get("postFee"));
            }
            // 实付金额
            if(searchMap.get("payMoney")!=null ){
                criteria.andEqualTo("payMoney",searchMap.get("payMoney"));
            }

        }
        return example;
    }

}
