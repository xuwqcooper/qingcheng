package com.qingcheng.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.qingcheng.dao.AdminMapper;
import com.qingcheng.dao.AdminRoleMapper;
import com.qingcheng.entity.GroupAdminRole;
import com.qingcheng.entity.PageResult;
import com.qingcheng.pojo.system.Admin;
import com.qingcheng.pojo.system.AdminRole;
import com.qingcheng.pojo.system.Menu;
import com.qingcheng.service.system.AdminService;
import com.qingcheng.util.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @Author: cooper
 * @Description:
 * @Date: Create in 18:50 2019/7/10
 */
@Service(interfaceClass = AdminService.class)
public class AdminServiceImpl  implements AdminService{

        @Autowired
        private AdminMapper adminMapper;

        @Autowired
        private AdminRoleMapper adminRoleMapper;

        /**
         * 返回全部记录
         * @return
         */
        public List<Admin> findAll() {
            return adminMapper.selectAll();
        }

        /**
         * 分页查询
         * @param page 页码
         * @param size 每页记录数
         * @return 分页结果
         */
        public PageResult<Admin> findPage(int page, int size) {
            PageHelper.startPage(page,size);
            Page<Admin> admins = (Page<Admin>) adminMapper.selectAll();
            return new PageResult<Admin>(admins.getTotal(),admins.getResult());
        }

        /**
         * 条件查询
         * @param searchMap 查询条件
         * @return
         */
        public List<Admin> findList(Map<String, Object> searchMap) {
            Example example = createExample(searchMap);
            return adminMapper.selectByExample(example);
        }

        /**
         * 分页+条件查询
         * @param searchMap
         * @param page
         * @param size
         * @return
         */
        public PageResult<Admin> findPage(Map<String, Object> searchMap, int page, int size) {
            PageHelper.startPage(page,size);
            Example example = createExample(searchMap);
            Page<Admin> admins = (Page<Admin>) adminMapper.selectByExample(example);
            return new PageResult<Admin>(admins.getTotal(),admins.getResult());
        }

        /**
         * 根据Id查询
         * @param id
         * @return
         */
        public GroupAdminRole findById(Integer id) {
            //根据主键查询admin
            Admin admin = adminMapper.selectByPrimaryKey(id);
            GroupAdminRole groupAdminRole = new GroupAdminRole();
            groupAdminRole.setAdmin(admin);
            Example example = new Example(AdminRole.class);
            Example.Criteria criteria = example.createCriteria();
            criteria.andEqualTo("adminId",id);
            List<AdminRole> adminRoles = adminRoleMapper.selectByExample(example);
            List list = new ArrayList<Integer>();
            for (AdminRole adminRole : adminRoles) {
                list.add(adminRole.getRoleId());
            }
            groupAdminRole.setRoleIds(list);
            return groupAdminRole;
        }


        /**
         * 修改
         * @param groupAdminRole
         */
        public void update(GroupAdminRole groupAdminRole) {
            Integer adminId = groupAdminRole.getAdmin().getId();
            Example example = new Example(AdminRole.class);
            Example.Criteria criteria = example.createCriteria();
            criteria.andEqualTo("adminId", adminId);
            adminRoleMapper.deleteByExample(example);
            //更新admin
            adminMapper.updateByPrimaryKeySelective(groupAdminRole.getAdmin());
            //循环添加roleId
            for (Integer roleId : groupAdminRole.getRoleIds()) {
                AdminRole adminRole = new AdminRole();
                adminRole.setAdminId(adminId);
                adminRole.setRoleId(roleId);
                adminRoleMapper.insert(adminRole);
            }

        }

        /**
         * 新增
         * @param groupAdminRole
         */
        @Transactional
        public void add(GroupAdminRole groupAdminRole) {
            //获取admin
            Admin admin = groupAdminRole.getAdmin();
            String password = admin.getPassword();
            //设置新密码
            String newPassword = BCrypt.hashpw(password, BCrypt.gensalt());
            admin.setPassword(newPassword);
            //添加admin
            adminMapper.insert(admin);
            List<Integer> roleIds = groupAdminRole.getRoleIds();
            Integer adminId = groupAdminRole.getAdmin().getId();
            for (Integer roleId : roleIds) {
                AdminRole adminRole = new AdminRole();
                adminRole.setAdminId(adminId);
                adminRole.setRoleId(roleId);
                adminRoleMapper.insert(adminRole);
            }
        }


        /**
         *  删除
         * @param id
         */
        public void delete(Integer id) {
            adminMapper.deleteByPrimaryKey(id);
        }

        /**
         * 构建查询条件
         * @param searchMap
         * @return
         */
        private Example createExample(Map<String, Object> searchMap){
            Example example=new Example(Admin.class);
            Example.Criteria criteria = example.createCriteria();
            if(searchMap!=null){
                // 用户名
                if(searchMap.get("loginName")!=null && !"".equals(searchMap.get("loginName"))){
                    criteria.andEqualTo("loginName",searchMap.get("loginName"));
                }
                // 密码
                if(searchMap.get("password")!=null && !"".equals(searchMap.get("password"))){
                    criteria.andLike("password","%"+searchMap.get("password")+"%");
                }
                // 状态
                if(searchMap.get("status")!=null && !"".equals(searchMap.get("status"))){
                    criteria.andEqualTo("status",searchMap.get("status"));
                }

                // id
                if(searchMap.get("id")!=null ){
                    criteria.andEqualTo("id",searchMap.get("id"));
                }

            }
            return example;
        }

    }


