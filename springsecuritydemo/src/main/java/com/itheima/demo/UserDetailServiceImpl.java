package com.itheima.demo;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: cooper
 * @Description:
 * @Date: Create in 20:03 2019/7/9
 */
public class UserDetailServiceImpl implements UserDetailsService {



    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        System.out.println("经过了userDetailService");
        //实际项目中应该从数据库中提取用户的角色列表
        List<GrantedAuthority> grantedAuthorities = new ArrayList<GrantedAuthority>();
        grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        return new User(s,"$2a$10$VqgG/lYAcAfJIHKn99ur4eLLsVnNYnNKiKeZmRlLBxAd5AY4787GC",grantedAuthorities );

    }
}
