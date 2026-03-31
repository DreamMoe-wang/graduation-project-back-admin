package com.example.admin.mapper;

import com.example.admin.entity.TradePostReview;
import org.apache.ibatis.annotations.Mapper;

/**
 * 发布审核记录 Mapper
 */
@Mapper
public interface TradePostReviewMapper {

    int insertTradePostReview(TradePostReview tradePostReview);
}
