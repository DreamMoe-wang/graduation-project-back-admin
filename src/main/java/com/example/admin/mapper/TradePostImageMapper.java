package com.example.admin.mapper;

import com.example.admin.entity.TradePostImage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 发布图片 Mapper
 */
@Mapper
public interface TradePostImageMapper {

    List<TradePostImage> selectByPostId(@Param("postId") Long postId);

    int insertTradePostImage(TradePostImage tradePostImage);

    int deleteByPostId(@Param("postId") Long postId);
}
