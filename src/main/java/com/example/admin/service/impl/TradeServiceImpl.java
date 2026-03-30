package com.example.admin.service.impl;

import cn.hutool.core.util.StrUtil;
import com.example.admin.common.PageResult;
import com.example.admin.dto.TradeQueryDTO;
import com.example.admin.dto.TradeSaveDTO;
import com.example.admin.service.TradeService;
import com.example.admin.vo.TradeOrderStatsVO;
import com.example.admin.vo.TradeOrderVO;
import com.example.admin.vo.TradeVO;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 交易模块服务实现
 *
 * <p>当前阶段先提供与前端页面匹配的占位数据，待数据库表创建完成后再接入 Mapper。</p>
 */
@Service
public class TradeServiceImpl implements TradeService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public PageResult<List<TradeVO>> getPublishPage(TradeQueryDTO queryDTO) {
        List<TradeVO> filteredList = filterTradeList(buildTradeSamples(), queryDTO, true);
        return toPage(filteredList, queryDTO.getPageNum(), queryDTO.getPageSize());
    }

    @Override
    public PageResult<List<TradeVO>> getTradeListPage(TradeQueryDTO queryDTO) {
        List<TradeVO> filteredList = filterTradeList(buildTradeSamples(), queryDTO, false);
        return toPage(filteredList, queryDTO.getPageNum(), queryDTO.getPageSize());
    }

    @Override
    public TradeVO getTradeDetail(Long id) {
        return buildTradeSamples().stream()
                .filter(item -> item.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("交易不存在"));
    }

    @Override
    public boolean createTrade(TradeSaveDTO tradeSaveDTO) {
        return true;
    }

    @Override
    public boolean updateTrade(Long id, TradeSaveDTO tradeSaveDTO) {
        getTradeDetail(id);
        return true;
    }

    @Override
    public boolean deleteTrade(Long id) {
        getTradeDetail(id);
        return true;
    }

    @Override
    public TradeOrderStatsVO getOrderStats() {
        List<TradeOrderVO> orderList = buildOrderSamples();
        int pendingCount = (int) orderList.stream().filter(item -> "pending".equals(item.getStatus())).count();
        int progressCount = (int) orderList.stream().filter(item -> "progress".equals(item.getStatus())).count();
        int successCount = (int) orderList.stream().filter(item -> "success".equals(item.getStatus())).count();
        return TradeOrderStatsVO.builder()
                .totalCount(orderList.size())
                .pendingCount(pendingCount)
                .progressCount(progressCount)
                .successCount(successCount)
                .build();
    }

    @Override
    public PageResult<List<TradeOrderVO>> getOrderPage(Integer pageNum, Integer pageSize, String status) {
        List<TradeOrderVO> orderList = buildOrderSamples();
        if (StrUtil.isNotBlank(status)) {
            orderList = orderList.stream()
                    .filter(item -> status.equals(item.getStatus()))
                    .collect(Collectors.toList());
        }
        return toPage(orderList, pageNum, pageSize);
    }

    @Override
    public TradeOrderVO getOrderDetail(Long id) {
        return buildOrderSamples().stream()
                .filter(item -> item.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("订单不存在"));
    }

    @Override
    public boolean receiveOrder(Long id) {
        getOrderDetail(id);
        return true;
    }

    @Override
    public boolean completeOrder(Long id) {
        getOrderDetail(id);
        return true;
    }

    @Override
    public boolean cancelOrder(Long id) {
        getOrderDetail(id);
        return true;
    }

    private List<TradeVO> filterTradeList(List<TradeVO> tradeList, TradeQueryDTO queryDTO, boolean checkTitle) {
        if (tradeList.isEmpty()) {
            return Collections.emptyList();
        }
        return tradeList.stream()
                .filter(item -> !checkTitle || StrUtil.isBlank(queryDTO.getTitle()) || StrUtil.contains(item.getTitle(), queryDTO.getTitle()))
                .filter(item -> StrUtil.isBlank(queryDTO.getStatus()) || queryDTO.getStatus().equals(item.getStatus()))
                .filter(item -> queryDTO.getMinAmount() == null || item.getAmount().compareTo(queryDTO.getMinAmount()) >= 0)
                .filter(item -> queryDTO.getMaxAmount() == null || item.getAmount().compareTo(queryDTO.getMaxAmount()) <= 0)
                .filter(item -> matchDateRange(item.getCreateTime(), queryDTO.getStartDate(), queryDTO.getEndDate()))
                .collect(Collectors.toList());
    }

    private boolean matchDateRange(String createTime, String startDate, String endDate) {
        if (StrUtil.isBlank(startDate) && StrUtil.isBlank(endDate)) {
            return true;
        }
        LocalDate tradeDate = LocalDate.parse(createTime.substring(0, 10), DATE_FORMATTER);
        if (StrUtil.isNotBlank(startDate)) {
            LocalDate start = LocalDate.parse(startDate, DATE_FORMATTER);
            if (tradeDate.isBefore(start)) {
                return false;
            }
        }
        if (StrUtil.isNotBlank(endDate)) {
            LocalDate end = LocalDate.parse(endDate, DATE_FORMATTER);
            if (tradeDate.isAfter(end)) {
                return false;
            }
        }
        return true;
    }

    private <T> PageResult<List<T>> toPage(List<T> sourceList, Integer pageNum, Integer pageSize) {
        int currentPage = pageNum == null || pageNum < 1 ? 1 : pageNum;
        int size = pageSize == null || pageSize < 1 ? 10 : pageSize;
        int fromIndex = Math.min((currentPage - 1) * size, sourceList.size());
        int toIndex = Math.min(fromIndex + size, sourceList.size());
        return PageResult.of((long) sourceList.size(), new ArrayList<>(sourceList.subList(fromIndex, toIndex)));
    }

    private List<TradeVO> buildTradeSamples() {
        return Arrays.asList(
                TradeVO.builder().id(1001L).title("上门维修服务 - 空调清洗").clientName("张先生").clientPhone("13800138001").workerName("李师傅").workerPhone("13900139001").amount(new BigDecimal("150.00")).status("trading").createTime("2024-01-15 10:30:00").description("需要清洗两台壁挂式空调").build(),
                TradeVO.builder().id(1002L).title("搬家服务 - 小型搬运").clientName("王女士").clientPhone("13800138002").workerName("").workerPhone("").amount(new BigDecimal("300.00")).status("published").createTime("2024-01-16 14:20:00").description("一居室搬家，有电梯").build(),
                TradeVO.builder().id(1003L).title("家教辅导 - 初中数学").clientName("刘先生").clientPhone("13800138003").workerName("陈老师").workerPhone("13900139003").amount(new BigDecimal("200.00")).status("trading").createTime("2024-01-17 09:00:00").description("每周两次，每次两小时").build(),
                TradeVO.builder().id(1004L).title("宠物寄养 - 猫咪照顾").clientName("赵女士").clientPhone("13800138004").workerName("").workerPhone("").amount(new BigDecimal("100.00")).status("auditing").createTime("2024-01-18 16:45:00").description("春节假期 7 天寄养").build(),
                TradeVO.builder().id(1005L).title("代驾服务 - 晚间代驾").clientName("孙先生").clientPhone("13800138005").workerName("周师傅").workerPhone("13900139005").amount(new BigDecimal("80.00")).status("completed").createTime("2024-01-19 20:00:00").description("从酒吧到小区").build(),
                TradeVO.builder().id(1006L).title("保洁服务 - 深度清洁").clientName("吴女士").clientPhone("13800138006").workerName("").workerPhone("").amount(new BigDecimal("250.00")).status("draft").createTime("2024-01-20 11:30:00").description("三居室全屋清洁").build(),
                TradeVO.builder().id(1007L).title("电脑维修 - 系统重装").clientName("郑先生").clientPhone("13800138007").workerName("钱工程师").workerPhone("13900139007").amount(new BigDecimal("120.00")).status("rejected").createTime("2024-01-21 13:15:00").description("笔记本系统重装，数据备份").build(),
                TradeVO.builder().id(1008L).title("管道疏通 - 厨房下水道").clientName("冯女士").clientPhone("13800138008").workerName("刘师傅").workerPhone("13900139008").amount(new BigDecimal("180.00")).status("trading").createTime("2024-01-22 09:30:00").description("厨房下水道堵塞").build(),
                TradeVO.builder().id(1009L).title("跑腿代购 - 超市采购").clientName("陈先生").clientPhone("13800138009").workerName("").workerPhone("").amount(new BigDecimal("50.00")).status("published").createTime("2024-01-23 15:00:00").description("帮忙购买生活用品").build(),
                TradeVO.builder().id(1010L).title("汽车保养 - 更换机油").clientName("杨先生").clientPhone("13800138010").workerName("黄技师").workerPhone("13900139010").amount(new BigDecimal("380.00")).status("completed").createTime("2024-01-24 10:00:00").description("全合成机油更换").build()
        );
    }

    private List<TradeOrderVO> buildOrderSamples() {
        return Arrays.asList(
                buildOrder(1L, "TR202603280001", "代取快递，送到宿舍楼下", "北京市海淀区", "2026-03-28 10:30", "20.00", "pending", "待接单"),
                buildOrder(2L, "TR202603280002", "专业保洁，上门打扫", "北京市朝阳区", "2026-03-28 09:15", "150.00", "progress", "进行中"),
                buildOrder(3L, "TR202603270003", "电脑维修，无法开机", "北京市东城区", "2026-03-27 16:20", "100.00", "success", "已完成"),
                buildOrder(4L, "TR202603270004", "宠物代遛 - 金毛犬", "北京市丰台区", "2026-03-27 14:40", "60.00", "pending", "待接单"),
                buildOrder(5L, "TR202603260005", "代买药品送上门", "北京市西城区", "2026-03-26 18:10", "35.00", "progress", "进行中"),
                buildOrder(6L, "TR202603260006", "文件打印装订", "北京市海淀区", "2026-03-26 11:20", "28.00", "success", "已完成")
        );
    }

    private TradeOrderVO buildOrder(Long id, String orderNo, String title, String area,
                                    String createTime, String price, String status, String statusText) {
        return TradeOrderVO.builder()
                .id(id)
                .orderNo(orderNo)
                .title(title)
                .area(area)
                .createTime(createTime)
                .price(new BigDecimal(price))
                .status(status)
                .statusText(statusText)
                .build();
    }
}
