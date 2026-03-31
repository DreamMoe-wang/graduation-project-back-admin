package com.example.admin.mapper;

import com.example.admin.dto.TradeQueryDTO;
import com.example.admin.entity.TradePost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 发布主表 Mapper
 */
@Mapper
public interface TradePostMapper {

    TradePost selectById(@Param("id") Long id);

    List<TradePost> selectPage(@Param("query") TradeQueryDTO queryDTO,
                               @Param("publisherId") Long publisherId,
                               @Param("onlyOwner") boolean onlyOwner,
                               @Param("publicOrOwner") boolean publicOrOwner,
                               @Param("offset") long offset,
                               @Param("pageSize") long pageSize);

    long countPage(@Param("query") TradeQueryDTO queryDTO,
                   @Param("publisherId") Long publisherId,
                   @Param("onlyOwner") boolean onlyOwner,
                   @Param("publicOrOwner") boolean publicOrOwner);

    long countActivePosts();

    int insertTradePost(TradePost tradePost);

    int updateTradePost(TradePost tradePost);

    int deleteById(@Param("id") Long id);
}
