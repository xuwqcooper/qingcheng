package com.qingcheng.service.system;

import com.qingcheng.entity.GroupAdminRole;
import com.qingcheng.pojo.system.AdminRole;

/**
 * @Author: cooper
 * @Description:
 * @Date: Create in 11:02 2019/7/10
 */
public interface AdminRoleService {

    public void add(GroupAdminRole groupAdminRole);

    public AdminRole findById(Integer id);
}
