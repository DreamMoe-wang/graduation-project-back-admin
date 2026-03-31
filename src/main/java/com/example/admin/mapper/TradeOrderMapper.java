package com.example.admin.mapper;

import com.example.admin.entity.TradeOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

/**
 * 订单 Mapper
 */
@Mapper
public interface TradeOrderMapper {

    TradeOrder selectById(@Param("id") Long id);

    TradeOrder selectLatestByPostId(@Param("postId") Long postId);

    List<TradeOrder> selectPage(@Param("status") Integer status,
                                @Param("currentUserId") Long currentUserId,
                                @Param("admin") boolean admin,
                                @Param("offset") long offset,
                                @Param("pageSize") long pageSize);

    long countPage(@Param("status") Integer status,
                   @Param("currentUserId") Long currentUserId,
                   @Param("admin") boolean admin);

    long countAll();

    BigDecimal sumCompletedAmount();

    int updateTradeOrder(TradeOrder tradeOrder);
}
