package com.qingcheng.controller.system;

import com.alibaba.dubbo.config.annotation.Reference;
import com.qingcheng.entity.GroupAdminRole;
import com.qingcheng.entity.PageResult;
import com.qingcheng.entity.Result;
import com.qingcheng.pojo.system.Admin;

import com.qingcheng.pojo.system.Menu;
import com.qingcheng.service.system.AdminRoleService;
import com.qingcheng.service.system.AdminService;

import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.web.bind.annotation.*;


import java.util.*;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Reference
    private AdminService adminService;

    @Reference
    private AdminRoleService adminRoleService;



    @GetMapping("/findAll")
    public List<Admin> findAll(){
        return adminService.findAll();
    }

    @GetMapping("/findPage")
    public PageResult<Admin> findPage(int page, int size){
        return adminService.findPage(page, size);
    }

    @PostMapping("/findList")
    public List<Admin> findList(@RequestBody Map<String,Object> searchMap){
        return adminService.findList(searchMap);
    }

    @PostMapping("/findPage")
    public PageResult<Admin> findPage(@RequestBody Map<String,Object> searchMap,int page, int size){
        return  adminService.findPage(searchMap,page,size);
    }

    @GetMapping("/findById")
    public GroupAdminRole findById(Integer id){
        GroupAdminRole groupAdminRole = adminService.findById(id);
        Admin admin = groupAdminRole.getAdmin();
        admin.setPassword(null);
        groupAdminRole.setAdmin(admin);
        return groupAdminRole;
    }


    @PostMapping("/add")
    public Result add(@RequestBody GroupAdminRole groupAdminRole){
        adminRoleService.add(groupAdminRole);
        return new Result();
    }

    @PostMapping("/update")
    public Result update(@RequestBody GroupAdminRole groupAdminRole){
        //获取admin
        Admin admin = groupAdminRole.getAdmin();
        String password = admin.getPassword();
        if (password != null && !"".equals(password)) {
            //设置加密密码
            String gensalt = BCrypt.gensalt();//获取盐
            String newPassword = BCrypt.hashpw(password, gensalt);
            admin.setPassword(newPassword);

        }
        adminService.update(groupAdminRole);
        return new Result();
    }

    @GetMapping("/delete")
    public Result delete(Integer id){
        adminService.delete(id);
        return new Result();
    }



}
