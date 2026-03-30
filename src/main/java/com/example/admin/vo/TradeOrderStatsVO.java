package com.example.admin.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 订单统计信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TradeOrderStatsVO {

    private Integer totalCount;

    private Integer pendingCount;

    private Integer progressCount;

    private Integer successCount;
}
