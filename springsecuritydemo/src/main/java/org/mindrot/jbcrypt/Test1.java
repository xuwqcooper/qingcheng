package org.mindrot.jbcrypt;

/**
 * @Author: cooper
 * @Description:
 * @Date: Create in 18:33 2019/7/9
 */
public class Test1 {
    public static void main(String[] args) {
        String gensalt = BCrypt.gensalt();//创建随机盐
        System.out.println(gensalt);
        String password = BCrypt.hashpw("123456", gensalt);
        System.out.println(password);


    }
}
