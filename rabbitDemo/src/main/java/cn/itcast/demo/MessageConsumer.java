package cn.itcast.demo;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;

/**
 * @Author: cooper
 * @Description:
 * @Date: Create in 1:07 2019/7/19
 */

/**
 * 消息监听类
 */
public class MessageConsumer implements MessageListener {
    public void onMessage(Message message) {
        System.out.println(new String(message.getBody()));

    }
}
