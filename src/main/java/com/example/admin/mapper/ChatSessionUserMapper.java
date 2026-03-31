package com.example.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.admin.entity.ChatSessionUser;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会话成员 Mapper
 */
@Mapper
public interface ChatSessionUserMapper extends BaseMapper<ChatSessionUser> {
}
