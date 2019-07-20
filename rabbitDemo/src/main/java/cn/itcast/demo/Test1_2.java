package cn.itcast.demo;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @Author: cooper
 * @Description:
 * @Date: Create in 1:00 2019/7/19
 */

/**
 * 分列模式(消息生产者)
 */
public class Test1_2 {
    public static void main(String[] args) {
        ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext-rabbitmq-producer.xml");
        RabbitTemplate rabbitTemplate =(RabbitTemplate) context.getBean("rabbitTemplate");
        rabbitTemplate.convertAndSend("exchange.fanout_test","","分列模式测试");
        ((ClassPathXmlApplicationContext)context).close();
    }
}
