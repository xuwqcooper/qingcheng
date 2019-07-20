package cn.itcast.demo;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @Author: cooper
 * @Description:
 * @Date: Create in 1:00 2019/7/19
 */
public class Test1 {
    public static void main(String[] args) {
        ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext-rabbitmq-producer.xml");
        RabbitTemplate rabbitTemplate =(RabbitTemplate) context.getBean("rabbitTemplate");
        rabbitTemplate.convertAndSend("","queue.test","直接模式测试");
        ((ClassPathXmlApplicationContext)context).close();
    }
}
