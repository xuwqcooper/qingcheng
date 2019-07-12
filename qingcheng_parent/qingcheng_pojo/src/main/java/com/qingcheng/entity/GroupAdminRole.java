package com.qingcheng.entity;

import com.qingcheng.pojo.system.Admin;

import java.io.Serializable;
import java.util.List;

/**
 * @Author: cooper
 * @Description:
 * @Date: Create in 10:02 2019/7/10
 */
public class GroupAdminRole implements Serializable {

    private Admin admin;

    private List<Integer> roleIds;

    public Admin getAdmin() {
        return admin;
    }

    public void setAdmin(Admin admin) {
        this.admin = admin;
    }

    public List<Integer> getRoleIds() {
        return roleIds;
    }

    public void setRoleIds(List<Integer> roleIds) {
        this.roleIds = roleIds;
    }
}
