package com.example.admin.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.admin.common.PageResult;
import com.example.admin.common.event.EventPublisher;
import com.example.admin.dto.TradeQueryDTO;
import com.example.admin.dto.TradeReviewDTO;
import com.example.admin.dto.TradeSaveDTO;
import com.example.admin.entity.TradeOrder;
import com.example.admin.entity.TradePost;
import com.example.admin.entity.TradePostReview;
import com.example.admin.entity.User;
import com.example.admin.mapper.TradeOrderMapper;
import com.example.admin.mapper.TradePostMapper;
import com.example.admin.mapper.TradePostReviewMapper;
import com.example.admin.mapper.UserMapper;
import com.example.admin.security.SecurityUtils;
import com.example.admin.service.TradeService;
import com.example.admin.vo.TradeOrderStatsVO;
import com.example.admin.vo.TradeOrderVO;
import com.example.admin.vo.TradeVO;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * 交易模块服务实现
 */
@Service
public class TradeServiceImpl implements TradeService {

    private static final Long DEFAULT_CURRENT_USER_ID = 1L;
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Resource
    private TradePostMapper tradePostMapper;

    @Resource
    private TradeOrderMapper tradeOrderMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private TradePostReviewMapper tradePostReviewMapper;

    @Resource
    private EventPublisher eventPublisher;

    @Override
    public PageResult<List<TradeVO>> getPublishPage(TradeQueryDTO queryDTO) {
        Page<TradePost> page = queryTradePostPage(queryDTO);
        List<TradeVO> records = page.getRecords().stream()
                .map(this::toTradeVO)
                .collect(Collectors.toList());
        return PageResult.of(page.getTotal(), records);
    }

    @Override
    public PageResult<List<TradeVO>> getTradeListPage(TradeQueryDTO queryDTO) {
        Page<TradePost> page = queryTradePostPage(queryDTO);
        List<TradeVO> records = page.getRecords().stream()
                .map(this::toTradeVO)
                .collect(Collectors.toList());
        return PageResult.of(page.getTotal(), records);
    }

    @Override
    public TradeVO getTradeDetail(Long id) {
        return toTradeVO(getTradePostById(id));
    }

    @Override
    public boolean createTrade(TradeSaveDTO tradeSaveDTO) {
        TradePost tradePost = new TradePost();
        Long currentUserId = SecurityUtils.getCurrentUserIdOrDefault(DEFAULT_CURRENT_USER_ID);
        tradePost.setPostNo(generatePostNo());
        tradePost.setPublisherId(currentUserId);
        tradePost.setPostType(1);
        fillTradePost(tradePost, tradeSaveDTO);
        Integer status = parseTradeStatus(tradeSaveDTO.getStatus());
        tradePost.setStatus(status == null ? 0 : status);
        applyPostStatusTime(tradePost, null);
        boolean success = tradePostMapper.insert(tradePost) > 0;
        if (success) {
            eventPublisher.publish("trade.post.created", tradePost.getPostNo());
        }
        return success;
    }

    @Override
    public boolean updateTrade(Long id, TradeSaveDTO tradeSaveDTO) {
        TradePost tradePost = getTradePostById(id);
        Integer oldStatus = tradePost.getStatus();
        fillTradePost(tradePost, tradeSaveDTO);
        Integer status = parseTradeStatus(tradeSaveDTO.getStatus());
        tradePost.setStatus(status == null ? tradePost.getStatus() : status);
        applyPostStatusTime(tradePost, oldStatus);
        boolean success = tradePostMapper.updateById(tradePost) > 0;
        if (success) {
            eventPublisher.publish("trade.post.updated", tradePost.getPostNo());
        }
        return success;
    }

    @Override
    public boolean deleteTrade(Long id) {
        getTradePostById(id);
        boolean success = tradePostMapper.deleteById(id) > 0;
        if (success) {
            eventPublisher.publish("trade.post.deleted", id);
        }
        return success;
    }

    @Override
    public boolean approveTrade(Long id, TradeReviewDTO reviewDTO) {
        TradePost tradePost = getTradePostById(id);
        Long currentUserId = SecurityUtils.getCurrentUserIdOrDefault(DEFAULT_CURRENT_USER_ID);
        tradePost.setStatus(3);
        tradePost.setReviewerId(currentUserId);
        tradePost.setReviewTime(LocalDateTime.now());
        tradePost.setReviewRemark(reviewDTO == null ? null : reviewDTO.getReviewRemark());
        tradePost.setPublishTime(LocalDateTime.now());
        tradePost.setOffShelfTime(null);
        saveReviewRecord(id, currentUserId, 1, reviewDTO == null ? null : reviewDTO.getReviewRemark());
        boolean success = tradePostMapper.updateById(tradePost) > 0;
        if (success) {
            eventPublisher.publish("trade.post.approved", tradePost.getPostNo());
        }
        return success;
    }

    @Override
    public boolean rejectTrade(Long id, TradeReviewDTO reviewDTO) {
        TradePost tradePost = getTradePostById(id);
        Long currentUserId = SecurityUtils.getCurrentUserIdOrDefault(DEFAULT_CURRENT_USER_ID);
        tradePost.setStatus(2);
        tradePost.setReviewerId(currentUserId);
        tradePost.setReviewTime(LocalDateTime.now());
        tradePost.setReviewRemark(reviewDTO == null ? null : reviewDTO.getReviewRemark());
        saveReviewRecord(id, currentUserId, 2, reviewDTO == null ? null : reviewDTO.getReviewRemark());
        boolean success = tradePostMapper.updateById(tradePost) > 0;
        if (success) {
            eventPublisher.publish("trade.post.rejected", tradePost.getPostNo());
        }
        return success;
    }

    @Override
    public TradeOrderStatsVO getOrderStats() {
        return TradeOrderStatsVO.builder()
                .totalCount(Math.toIntExact(tradeOrderMapper.selectCount(null)))
                .pendingCount(countOrderByStatus(0))
                .progressCount(countOrderByStatus(1))
                .successCount(countOrderByStatus(2))
                .build();
    }

    @Override
    public PageResult<List<TradeOrderVO>> getOrderPage(Integer pageNum, Integer pageSize, String status) {
        LambdaQueryWrapper<TradeOrder> wrapper = new LambdaQueryWrapper<>();
        Integer orderStatus = parseOrderStatus(status);
        if (orderStatus != null) {
            wrapper.eq(TradeOrder::getStatus, orderStatus);
        }
        wrapper.orderByDesc(TradeOrder::getCreateTime);

        Page<TradeOrder> page = new Page<>(pageNum == null || pageNum < 1 ? 1 : pageNum,
                pageSize == null || pageSize < 1 ? 10 : pageSize);
        Page<TradeOrder> result = tradeOrderMapper.selectPage(page, wrapper);
        List<TradeOrderVO> records = result.getRecords().stream()
                .map(this::toTradeOrderVO)
                .collect(Collectors.toList());
        return PageResult.of(result.getTotal(), records);
    }

    @Override
    public TradeOrderVO getOrderDetail(Long id) {
        return toTradeOrderVO(getTradeOrderById(id));
    }

    @Override
    public boolean receiveOrder(Long id) {
        TradeOrder order = getTradeOrderById(id);
        Long currentUserId = SecurityUtils.getCurrentUserIdOrDefault(DEFAULT_CURRENT_USER_ID);
        if (order.getStatus() != 0) {
            throw new RuntimeException("当前订单状态不允许接单");
        }
        order.setStatus(1);
        if (order.getReceiverId() == null || order.getReceiverId() <= 0) {
            order.setReceiverId(currentUserId);
        }
        order.setConfirmTime(LocalDateTime.now());
        boolean success = tradeOrderMapper.updateById(order) > 0;
        if (success) {
            eventPublisher.publish("trade.order.received", order.getOrderNo());
        }
        return success;
    }

    @Override
    public boolean completeOrder(Long id) {
        TradeOrder order = getTradeOrderById(id);
        if (order.getStatus() != 1) {
            throw new RuntimeException("只有进行中的订单才能完成");
        }
        order.setStatus(2);
        order.setFinishTime(LocalDateTime.now());
        boolean updated = tradeOrderMapper.updateById(order) > 0;
        TradePost tradePost = tradePostMapper.selectById(order.getPostId());
        if (updated && tradePost != null) {
            tradePost.setStatus(4);
            tradePost.setOffShelfTime(LocalDateTime.now());
            tradePostMapper.updateById(tradePost);
        }
        if (updated) {
            eventPublisher.publish("trade.order.completed", order.getOrderNo());
        }
        return updated;
    }

    @Override
    public boolean cancelOrder(Long id) {
        TradeOrder order = getTradeOrderById(id);
        if (order.getStatus() == 2) {
            throw new RuntimeException("已完成订单不能取消");
        }
        order.setStatus(3);
        order.setCancelTime(LocalDateTime.now());
        if (StrUtil.isBlank(order.getCancelReason())) {
            order.setCancelReason("后台取消订单");
        }
        boolean success = tradeOrderMapper.updateById(order) > 0;
        if (success) {
            eventPublisher.publish("trade.order.cancelled", order.getOrderNo());
        }
        return success;
    }

    private Page<TradePost> queryTradePostPage(TradeQueryDTO queryDTO) {
        LambdaQueryWrapper<TradePost> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(queryDTO.getTitle())) {
            wrapper.like(TradePost::getTitle, queryDTO.getTitle().trim());
        }
        Integer tradeStatus = parseTradeStatus(queryDTO.getStatus());
        if (tradeStatus != null) {
            wrapper.eq(TradePost::getStatus, tradeStatus);
        }
        if (queryDTO.getMinAmount() != null) {
            wrapper.ge(TradePost::getPrice, queryDTO.getMinAmount());
        }
        if (queryDTO.getMaxAmount() != null) {
            wrapper.le(TradePost::getPrice, queryDTO.getMaxAmount());
        }
        if (StrUtil.isNotBlank(queryDTO.getStartDate())) {
            wrapper.ge(TradePost::getCreateTime,
                    LocalDate.parse(queryDTO.getStartDate()).atStartOfDay());
        }
        if (StrUtil.isNotBlank(queryDTO.getEndDate())) {
            wrapper.le(TradePost::getCreateTime,
                    LocalDate.parse(queryDTO.getEndDate()).atTime(LocalTime.MAX));
        }
        wrapper.orderByDesc(TradePost::getCreateTime);

        Page<TradePost> page = new Page<>(queryDTO.getPageNum() == null || queryDTO.getPageNum() < 1 ? 1 : queryDTO.getPageNum(),
                queryDTO.getPageSize() == null || queryDTO.getPageSize() < 1 ? 10 : queryDTO.getPageSize());
        return tradePostMapper.selectPage(page, wrapper);
    }

    private TradeVO toTradeVO(TradePost tradePost) {
        TradeOrder relatedOrder = findLatestOrderByPostId(tradePost.getId());
        User receiver = relatedOrder == null ? null : userMapper.selectById(relatedOrder.getReceiverId());

        return TradeVO.builder()
                .id(tradePost.getId())
                .title(tradePost.getTitle())
                .clientName(tradePost.getContactName())
                .clientPhone(tradePost.getContactPhone())
                .workerName(receiver == null ? null : buildDisplayName(receiver))
                .workerPhone(receiver == null ? null : receiver.getPhone())
                .amount(tradePost.getPrice())
                .status(formatTradeStatus(tradePost.getStatus()))
                .createTime(formatDateTime(tradePost.getCreateTime()))
                .description(tradePost.getContent())
                .build();
    }

    private TradeOrderVO toTradeOrderVO(TradeOrder order) {
        TradePost tradePost = tradePostMapper.selectById(order.getPostId());
        String area = "";
        String title = "";
        if (tradePost != null) {
            title = tradePost.getTitle();
            area = buildArea(tradePost.getCityName(), tradePost.getAreaName());
        }
        return TradeOrderVO.builder()
                .id(order.getId())
                .orderNo(order.getOrderNo())
                .title(title)
                .area(area)
                .createTime(formatDateTime(order.getCreateTime()))
                .price(order.getAmount())
                .status(formatOrderStatus(order.getStatus()))
                .statusText(formatOrderStatusText(order.getStatus()))
                .build();
    }

    private TradePost getTradePostById(Long id) {
        TradePost tradePost = tradePostMapper.selectById(id);
        if (tradePost == null) {
            throw new RuntimeException("交易不存在");
        }
        return tradePost;
    }

    private TradeOrder getTradeOrderById(Long id) {
        TradeOrder order = tradeOrderMapper.selectById(id);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        return order;
    }

    private TradeOrder findLatestOrderByPostId(Long postId) {
        LambdaQueryWrapper<TradeOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TradeOrder::getPostId, postId)
                .orderByDesc(TradeOrder::getCreateTime)
                .last("limit 1");
        return tradeOrderMapper.selectOne(wrapper);
    }

    private int countOrderByStatus(Integer status) {
        LambdaQueryWrapper<TradeOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TradeOrder::getStatus, status);
        return Math.toIntExact(tradeOrderMapper.selectCount(wrapper));
    }

    private void fillTradePost(TradePost tradePost, TradeSaveDTO tradeSaveDTO) {
        tradePost.setTitle(tradeSaveDTO.getTitle());
        tradePost.setContent(tradeSaveDTO.getDescription());
        tradePost.setPrice(tradeSaveDTO.getAmount());
        tradePost.setContactName(tradeSaveDTO.getClientName());
        tradePost.setContactPhone(tradeSaveDTO.getClientPhone());
    }

    private void saveReviewRecord(Long postId, Long reviewerId, Integer reviewResult, String reviewRemark) {
        TradePostReview review = new TradePostReview();
        review.setPostId(postId);
        review.setReviewerId(reviewerId);
        review.setReviewResult(reviewResult);
        review.setReviewRemark(reviewRemark);
        tradePostReviewMapper.insert(review);
    }

    private void applyPostStatusTime(TradePost tradePost, Integer oldStatus) {
        if (tradePost.getStatus() != null && tradePost.getStatus() == 3 && (oldStatus == null || oldStatus != 3)) {
            tradePost.setPublishTime(LocalDateTime.now());
            tradePost.setOffShelfTime(null);
        }
        if (tradePost.getStatus() != null && tradePost.getStatus() == 4 && (oldStatus == null || oldStatus != 4)) {
            tradePost.setOffShelfTime(LocalDateTime.now());
        }
    }

    private String buildDisplayName(User user) {
        return StrUtil.isNotBlank(user.getNickname()) ? user.getNickname() : user.getUsername();
    }

    private String buildArea(String cityName, String areaName) {
        if (StrUtil.isBlank(cityName) && StrUtil.isBlank(areaName)) {
            return "";
        }
        if (StrUtil.isBlank(cityName)) {
            return areaName;
        }
        if (StrUtil.isBlank(areaName)) {
            return cityName;
        }
        return cityName + areaName;
    }

    private String formatDateTime(LocalDateTime dateTime) {
        return dateTime == null ? null : dateTime.format(DATETIME_FORMATTER);
    }

    private String generatePostNo() {
        return "TP" + System.currentTimeMillis();
    }

    private Integer parseTradeStatus(String status) {
        if (StrUtil.isBlank(status)) {
            return null;
        }
        String value = status.trim().toLowerCase(Locale.ROOT);
        switch (value) {
            case "draft":
                return 0;
            case "auditing":
                return 1;
            case "rejected":
                return 2;
            case "published":
            case "trading":
            case "onshelf":
            case "on_shelf":
                return 3;
            case "offshelf":
            case "off_shelf":
            case "completed":
                return 4;
            default:
                if (StrUtil.isNumeric(value)) {
                    return Integer.parseInt(value);
                }
                return null;
        }
    }

    private String formatTradeStatus(Integer status) {
        if (status == null) {
            return null;
        }
        switch (status) {
            case 0:
                return "draft";
            case 1:
                return "auditing";
            case 2:
                return "rejected";
            case 3:
                return "published";
            case 4:
                return "offShelf";
            default:
                return String.valueOf(status);
        }
    }

    private Integer parseOrderStatus(String status) {
        if (StrUtil.isBlank(status)) {
            return null;
        }
        String value = status.trim().toLowerCase(Locale.ROOT);
        switch (value) {
            case "pending":
                return 0;
            case "progress":
            case "processing":
                return 1;
            case "success":
            case "completed":
                return 2;
            case "cancel":
            case "cancelled":
                return 3;
            default:
                if (StrUtil.isNumeric(value)) {
                    return Integer.parseInt(value);
                }
                return null;
        }
    }

    private String formatOrderStatus(Integer status) {
        if (status == null) {
            return null;
        }
        switch (status) {
            case 0:
                return "pending";
            case 1:
                return "progress";
            case 2:
                return "success";
            case 3:
                return "cancel";
            default:
                return String.valueOf(status);
        }
    }

    private String formatOrderStatusText(Integer status) {
        if (status == null) {
            return "";
        }
        switch (status) {
            case 0:
                return "待确认";
            case 1:
                return "进行中";
            case 2:
                return "已完成";
            case 3:
                return "已取消";
            default:
                return "未知状态";
        }
    }
}
