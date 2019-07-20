package cn.itcast.demo;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @Author: cooper
 * @Description:
 * @Date: Create in 1:10 2019/7/19
 */
public class Test2 {
    public static void main(String[] args) {
        ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext-rabbitmq-consumer.xml");

    }
}
