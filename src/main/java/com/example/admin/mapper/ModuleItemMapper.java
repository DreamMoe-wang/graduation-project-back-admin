package com.example.admin.mapper;

import com.example.admin.entity.ModuleItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 通用模块数据 Mapper
 */
@Mapper
public interface ModuleItemMapper {

    ModuleItem selectById(@Param("id") Long id, @Param("moduleName") String moduleName);

    List<ModuleItem> selectPage(@Param("moduleName") String moduleName,
                                @Param("offset") long offset,
                                @Param("pageSize") long pageSize);

    long countPage(@Param("moduleName") String moduleName);

    int insertModuleItem(ModuleItem moduleItem);

    int updateModuleItem(ModuleItem moduleItem);

    int deleteById(@Param("id") Long id, @Param("moduleName") String moduleName);

    int cleanByModuleName(@Param("moduleName") String moduleName);
}
