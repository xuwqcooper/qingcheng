package com.qingcheng.consumer;

import com.alibaba.fastjson.JSON;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @Author: cooper
 * @Description:
 * @Date: Create in 19:36 2019/7/20
 */
@Component
public class SmsMessageConsumer implements MessageListener {
    public void onMessage(Message message) {
        String jsonString = new String(message.getBody());//之前存在mq里面转化为json字符串的map
        Map<String,String> map = JSON.parseObject(jsonString, Map.class);
        String phone = map.get("phone");//手机号
        String code = map.get("code");//验证码
        System.out.println("手机号:"+phone+"验证码: "+code);

        //调用阿里云通信..
    }
}
