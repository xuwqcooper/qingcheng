package com.qingcheng.pojo.system;

import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * @Author: cooper
 * @Description:
 * @Date: Create in 9:54 2019/7/10
 */
@Table(name = "tb_admin_role")
public class AdminRole implements Serializable {

    @Id
    private Integer adminId;

    @Id
    private Integer roleId;

    public Integer getAdminId() {
        return adminId;
    }

    public void setAdminId(Integer adminId) {
        this.adminId = adminId;
    }

    public Integer getRoleId() {
        return roleId;
    }

    public void setRoleId(Integer roleId) {
        this.roleId = roleId;
    }
}