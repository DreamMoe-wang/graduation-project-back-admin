package com.example.admin.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.admin.dto.UserDTO;
import com.example.admin.entity.User;

import java.util.List;

/**
 * 用户服务接口
 */
public interface UserService {

    /**
     * 根据 ID 查询用户
     */
    User getById(Long id);

    /**
     * 根据用户名查询用户
     */
    User getByUsername(String username);

    /**
     * 分页查询用户列表
     */
    Page<User> pageList(int pageNum, int pageSize);

    /**
     * 获取所有用户列表
     */
    List<User> list();

    /**
     * 创建用户
     */
    boolean create(UserDTO userDTO);

    /**
     * 更新用户
     */
    boolean update(Long id, UserDTO userDTO);

    /**
     * 删除用户
     */
    boolean delete(Long id);

    /**
     * 用户登录
     */
    String login(String username, String password);
}
