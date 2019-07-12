package org.mindrot.jbcrypt;

/**
 * @Author: cooper
 * @Description:
 * @Date: Create in 18:38 2019/7/9
 */
public class Test2 {
    public static void main(String[] args) {

        boolean checkpw = BCrypt.checkpw("123456", "$2a$10$HKNYM95im1/93MHd2wj1z.URZoZcpSnx/P0ZgeBWZK1T2x7E/65.y");
        System.out.println(checkpw);
    }
}
