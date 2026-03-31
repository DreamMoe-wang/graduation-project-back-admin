package com.example.admin.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.admin.dto.LoginDTO;
import com.example.admin.dto.UserDTO;
import com.example.admin.entity.User;
import com.example.admin.mapper.UserMapper;
import com.example.admin.service.AuthService;
import com.example.admin.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 用户服务实现类
 */
@Service
public class UserServiceImpl implements UserService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private AuthService authService;

    @Override
    public User getById(Long id) {
        return userMapper.selectById(id);
    }

    @Override
    public User getByUsername(String username) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        return userMapper.selectOne(wrapper);
    }

    @Override
    public Page<User> pageList(int pageNum, int pageSize) {
        Page<User> page = new Page<>(pageNum, pageSize);
        return userMapper.selectPage(page, null);
    }

    @Override
    public List<User> list() {
        return userMapper.selectList(null);
    }

    @Override
    public boolean create(UserDTO userDTO) {
        User user = new User();
        BeanUtils.copyProperties(userDTO, user);
        
        // 加密密码
        if (StrUtil.isNotBlank(userDTO.getPassword())) {
            user.setPassword(BCrypt.hashpw(userDTO.getPassword()));
        }
        
        // 设置默认状态
        if (user.getStatus() == null) {
            user.setStatus(1);
        }
        
        return userMapper.insert(user) > 0;
    }

    @Override
    public boolean update(Long id, UserDTO userDTO) {
        User user = getById(id);
        if (user == null) {
            return false;
        }
        
        BeanUtils.copyProperties(userDTO, user, "password");
        
        // 如果提供了新密码，则更新
        if (StrUtil.isNotBlank(userDTO.getPassword())) {
            user.setPassword(BCrypt.hashpw(userDTO.getPassword()));
        }
        
        return userMapper.updateById(user) > 0;
    }

    @Override
    public boolean delete(Long id) {
        return userMapper.deleteById(id) > 0;
    }

    @Override
    public String login(String username, String password) {
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername(username);
        loginDTO.setPassword(password);
        return authService.login(loginDTO).getToken();
    }
}
