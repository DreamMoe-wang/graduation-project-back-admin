package com.example.admin.service.impl;

import cn.hutool.core.util.StrUtil;
import com.example.admin.common.PageResult;
import com.example.admin.common.assembler.UserProfileAssembler;
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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * 交易模块服务实现
 */
@Service
public class TradeServiceImpl implements TradeService {

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

    @Resource
    private UserProfileAssembler userProfileAssembler;

    @Override
    public PageResult<List<TradeVO>> getPublishPage(TradeQueryDTO queryDTO) {
        TradeQueryDTO normalizedQuery = normalizeTradeQuery(queryDTO);
        Long currentUserId = currentUserId();
        boolean admin = isAdmin();
        long total = tradePostMapper.countPage(normalizedQuery, currentUserId, !admin, false, false);
        List<TradeVO> records = tradePostMapper.selectPage(
                        normalizedQuery,
                        currentUserId,
                        !admin,
                        false,
                        false,
                        offset(normalizedQuery.getPageNum(), normalizedQuery.getPageSize()),
                        pageSize(normalizedQuery.getPageSize()))
                .stream()
                .map(this::toTradeVO)
                .collect(Collectors.toList());
        return PageResult.of(total, records);
    }

    @Override
    public PageResult<List<TradeVO>> getTradeListPage(TradeQueryDTO queryDTO) {
        TradeQueryDTO normalizedQuery = normalizeTradeQuery(queryDTO);
        Long currentUserId = currentUserId();
        boolean admin = isAdmin();
        if (!admin) {
            normalizedQuery.setStatus(null);
        }
        long total = tradePostMapper.countPage(normalizedQuery, currentUserId, false, false, !admin);
        List<TradeVO> records = tradePostMapper.selectPage(
                        normalizedQuery,
                        currentUserId,
                        false,
                        false,
                        !admin,
                        offset(normalizedQuery.getPageNum(), normalizedQuery.getPageSize()),
                        pageSize(normalizedQuery.getPageSize()))
                .stream()
                .map(this::toTradeVO)
                .collect(Collectors.toList());
        return PageResult.of(total, records);
    }

    @Override
    public TradeVO getTradeDetail(Long id) {
        TradePost tradePost = getTradePostById(id);
        assertTradePostAccessible(tradePost);
        return toTradeVO(tradePost);
    }

    @Override
    public boolean createTrade(TradeSaveDTO tradeSaveDTO) {
        Long currentUserId = currentUserId();

        TradePost tradePost = new TradePost();
        tradePost.setPostNo(generatePostNo());
        tradePost.setPublisherId(currentUserId);
        tradePost.setPostType(1);
        tradePost.setDeleted(0);
        fillTradePost(tradePost, tradeSaveDTO);
        Integer status = parseTradeStatus(tradeSaveDTO.getStatus());
        tradePost.setStatus(status == null ? 0 : status);
        applyPostStatusTime(tradePost, null);

        boolean success = tradePostMapper.insertTradePost(tradePost) > 0;
        if (success) {
            eventPublisher.publish("trade.post.created", tradePost.getPostNo());
        }
        return success;
    }

    @Override
    public boolean updateTrade(Long id, TradeSaveDTO tradeSaveDTO) {
        TradePost tradePost = getTradePostById(id);
        assertTradePostOwner(tradePost);

        Integer oldStatus = tradePost.getStatus();
        fillTradePost(tradePost, tradeSaveDTO);
        Integer status = parseTradeStatus(tradeSaveDTO.getStatus());
        tradePost.setStatus(status == null ? tradePost.getStatus() : status);
        applyPostStatusTime(tradePost, oldStatus);

        boolean success = tradePostMapper.updateTradePost(tradePost) > 0;
        if (success) {
            eventPublisher.publish("trade.post.updated", tradePost.getPostNo());
        }
        return success;
    }

    @Override
    public boolean deleteTrade(Long id) {
        TradePost tradePost = getTradePostById(id);
        assertTradePostOwner(tradePost);

        boolean success = tradePostMapper.deleteById(id) > 0;
        if (success) {
            eventPublisher.publish("trade.post.deleted", id);
        }
        return success;
    }

    @Override
    public boolean approveTrade(Long id, TradeReviewDTO reviewDTO) {
        assertAdmin();
        Long currentUserId = currentUserId();

        TradePost tradePost = getTradePostById(id);
        tradePost.setStatus(3);
        tradePost.setReviewerId(currentUserId);
        tradePost.setReviewTime(LocalDateTime.now());
        tradePost.setReviewRemark(reviewDTO == null ? null : reviewDTO.getReviewRemark());
        tradePost.setPublishTime(LocalDateTime.now());
        tradePost.setOffShelfTime(null);

        saveReviewRecord(id, currentUserId, 1, reviewDTO == null ? null : reviewDTO.getReviewRemark());
        boolean success = tradePostMapper.updateTradePost(tradePost) > 0;
        if (success) {
            eventPublisher.publish("trade.post.approved", tradePost.getPostNo());
        }
        return success;
    }

    @Override
    public boolean rejectTrade(Long id, TradeReviewDTO reviewDTO) {
        assertAdmin();
        Long currentUserId = currentUserId();

        TradePost tradePost = getTradePostById(id);
        tradePost.setStatus(2);
        tradePost.setReviewerId(currentUserId);
        tradePost.setReviewTime(LocalDateTime.now());
        tradePost.setReviewRemark(reviewDTO == null ? null : reviewDTO.getReviewRemark());

        saveReviewRecord(id, currentUserId, 2, reviewDTO == null ? null : reviewDTO.getReviewRemark());
        boolean success = tradePostMapper.updateTradePost(tradePost) > 0;
        if (success) {
            eventPublisher.publish("trade.post.rejected", tradePost.getPostNo());
        }
        return success;
    }

    @Override
    public boolean receiveTradePost(Long postId) {
        Long currentUserId = currentUserId();
        TradePost tradePost = getTradePostById(postId);
        if (tradePost.getStatus() == null || tradePost.getStatus() != 3) {
            throw new RuntimeException("当前交易不可接取");
        }
        if (!isAdmin() && currentUserId.equals(tradePost.getPublisherId())) {
            throw new AccessDeniedException("不能接取自己发布的交易");
        }

        TradeOrder latestOrder = tradeOrderMapper.selectLatestByPostId(postId);
        TradeOrder order;
        boolean createOrder = latestOrder == null || latestOrder.getStatus() == null || latestOrder.getStatus() == 3;
        if (createOrder) {
            order = new TradeOrder();
            order.setOrderNo(generateOrderNo());
            order.setPostId(tradePost.getId());
            order.setPublisherId(tradePost.getPublisherId());
            order.setAmount(tradePost.getPrice());
            order.setRemark("用户从交易大全接取交易");
        } else {
            if (latestOrder.getStatus() == 1 || latestOrder.getStatus() == 2) {
                throw new RuntimeException("当前交易已被接取");
            }
            order = latestOrder;
        }

        order.setReceiverId(currentUserId);
        order.setStatus(1);
        order.setConfirmTime(LocalDateTime.now());
        order.setFinishTime(null);
        order.setCancelTime(null);
        order.setCancelReason(null);

        boolean success = createOrder
                ? tradeOrderMapper.insertTradeOrder(order) > 0
                : tradeOrderMapper.updateTradeOrder(order) > 0;
        if (success) {
            tradePost.setStatus(4);
            tradePostMapper.updateTradePost(tradePost);
            eventPublisher.publish("trade.post.received", tradePost.getPostNo());
        }
        return success;
    }

    @Override
    public TradeOrderStatsVO getOrderStats() {
        Long currentUserId = currentUserId();
        boolean admin = isAdmin();
        return TradeOrderStatsVO.builder()
                .totalCount(Math.toIntExact(tradeOrderMapper.countPage(null, currentUserId, admin)))
                .pendingCount(Math.toIntExact(tradeOrderMapper.countPage(0, currentUserId, admin)))
                .progressCount(Math.toIntExact(tradeOrderMapper.countPage(1, currentUserId, admin)))
                .successCount(Math.toIntExact(tradeOrderMapper.countPage(2, currentUserId, admin)))
                .build();
    }

    @Override
    public PageResult<List<TradeOrderVO>> getOrderPage(Integer pageNum, Integer pageSize, String status) {
        Integer orderStatus = parseOrderStatus(status);
        Long currentUserId = currentUserId();
        boolean admin = isAdmin();
        long total = tradeOrderMapper.countPage(orderStatus, currentUserId, admin);
        List<TradeOrderVO> records = tradeOrderMapper.selectPage(
                        orderStatus,
                        currentUserId,
                        admin,
                        offset(pageNum, pageSize),
                        pageSize(pageSize))
                .stream()
                .map(this::toTradeOrderVO)
                .collect(Collectors.toList());
        return PageResult.of(total, records);
    }

    @Override
    public TradeOrderVO getOrderDetail(Long id) {
        TradeOrder order = getTradeOrderById(id);
        assertOrderAccessible(order);
        return toTradeOrderVO(order);
    }

    @Override
    public boolean receiveOrder(Long id) {
        Long currentUserId = currentUserId();
        TradeOrder order = getTradeOrderById(id);
        if (order.getStatus() != 0) {
            throw new RuntimeException("当前订单状态不允许接单");
        }
        if (!isAdmin() && currentUserId.equals(order.getPublisherId())) {
            throw new AccessDeniedException("不能接收自己发布的订单");
        }
        if (!isAdmin() && order.getReceiverId() != null && order.getReceiverId() > 0
                && !currentUserId.equals(order.getReceiverId())) {
            throw new AccessDeniedException("该订单已分配给其他用户");
        }

        order.setStatus(1);
        order.setReceiverId(currentUserId);
        order.setConfirmTime(LocalDateTime.now());

        boolean success = tradeOrderMapper.updateTradeOrder(order) > 0;
        if (success) {
            TradePost tradePost = tradePostMapper.selectById(order.getPostId());
            if (tradePost != null) {
                tradePost.setStatus(4);
                tradePostMapper.updateTradePost(tradePost);
            }
            eventPublisher.publish("trade.order.received", order.getOrderNo());
        }
        return success;
    }

    @Override
    public boolean completeOrder(Long id) {
        TradeOrder order = getTradeOrderById(id);
        assertOrderAccessible(order);
        if (order.getStatus() != 1) {
            throw new RuntimeException("只有进行中的订单才能完成");
        }

        order.setStatus(2);
        order.setFinishTime(LocalDateTime.now());
        boolean updated = tradeOrderMapper.updateTradeOrder(order) > 0;

        TradePost tradePost = tradePostMapper.selectById(order.getPostId());
        if (updated && tradePost != null) {
            tradePost.setStatus(5);
            tradePost.setOffShelfTime(LocalDateTime.now());
            tradePostMapper.updateTradePost(tradePost);
        }
        if (updated) {
            eventPublisher.publish("trade.order.completed", order.getOrderNo());
        }
        return updated;
    }

    @Override
    public boolean cancelOrder(Long id) {
        TradeOrder order = getTradeOrderById(id);
        assertOrderAccessible(order);
        if (order.getStatus() == 2) {
            throw new RuntimeException("已完成订单不能取消");
        }

        order.setStatus(3);
        order.setCancelTime(LocalDateTime.now());
        if (StrUtil.isBlank(order.getCancelReason())) {
            order.setCancelReason("用户取消订单");
        }

        boolean success = tradeOrderMapper.updateTradeOrder(order) > 0;
        if (success) {
            TradePost tradePost = tradePostMapper.selectById(order.getPostId());
            if (tradePost != null) {
                tradePost.setStatus(3);
                tradePost.setOffShelfTime(null);
                tradePostMapper.updateTradePost(tradePost);
            }
            eventPublisher.publish("trade.order.cancelled", order.getOrderNo());
        }
        return success;
    }

    private TradeVO toTradeVO(TradePost tradePost) {
        User publisher = userMapper.selectById(tradePost.getPublisherId());
        TradeOrder relatedOrder = tradeOrderMapper.selectLatestByPostId(tradePost.getId());
        User receiver = relatedOrder == null || relatedOrder.getReceiverId() == null
                ? null
                : userMapper.selectById(relatedOrder.getReceiverId());

        return TradeVO.builder()
                .id(tradePost.getId())
                .title(tradePost.getTitle())
                .clientName(buildDisplayName(publisher))
                .clientPhone(tradePost.getContactPhone())
                .workerName(receiver == null ? null : buildDisplayName(receiver))
                .workerPhone(receiver == null ? null : receiver.getPhone())
                .publisher(userProfileAssembler.toProfile(publisher))
                .worker(userProfileAssembler.toProfile(receiver))
                .amount(tradePost.getPrice())
                .status(formatTradeStatus(tradePost.getStatus()))
                .createTime(formatDateTime(tradePost.getCreateTime()))
                .description(tradePost.getContent())
                .build();
    }

    private TradeOrderVO toTradeOrderVO(TradeOrder order) {
        TradePost tradePost = tradePostMapper.selectById(order.getPostId());
        User publisher = userMapper.selectById(order.getPublisherId());
        User receiver = order.getReceiverId() == null ? null : userMapper.selectById(order.getReceiverId());
        String title = tradePost == null ? "" : tradePost.getTitle();
        String area = tradePost == null ? "" : buildArea(tradePost.getCityName(), tradePost.getAreaName());

        return TradeOrderVO.builder()
                .id(order.getId())
                .orderNo(order.getOrderNo())
                .title(title)
                .area(area)
                .publisher(userProfileAssembler.toProfile(publisher))
                .receiver(userProfileAssembler.toProfile(receiver))
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

    private void assertTradePostAccessible(TradePost tradePost) {
        if (isAdmin()) {
            return;
        }
        Long currentUserId = currentUserId();
        boolean owner = currentUserId.equals(tradePost.getPublisherId());
        boolean published = tradePost.getStatus() != null && tradePost.getStatus() == 3;
        TradeOrder relatedOrder = tradeOrderMapper.selectLatestByPostId(tradePost.getId());
        boolean participant = relatedOrder != null
                && (currentUserId.equals(relatedOrder.getPublisherId()) || currentUserId.equals(relatedOrder.getReceiverId()));
        if (!owner && !published && !participant) {
            throw new AccessDeniedException("无权查看该交易");
        }
    }

    private void assertTradePostOwner(TradePost tradePost) {
        if (isAdmin()) {
            return;
        }
        if (!currentUserId().equals(tradePost.getPublisherId())) {
            throw new AccessDeniedException("只能操作自己发布的交易");
        }
    }

    private void assertOrderAccessible(TradeOrder order) {
        if (isAdmin()) {
            return;
        }
        Long currentUserId = currentUserId();
        if (!currentUserId.equals(order.getPublisherId()) && !currentUserId.equals(order.getReceiverId())) {
            throw new AccessDeniedException("无权访问该订单");
        }
    }

    private void assertAdmin() {
        if (!isAdmin()) {
            throw new AccessDeniedException("仅管理员可执行该操作");
        }
    }

    private void fillTradePost(TradePost tradePost, TradeSaveDTO tradeSaveDTO) {
        tradePost.setTitle(tradeSaveDTO.getTitle());
        tradePost.setContent(tradeSaveDTO.getDescription());
        tradePost.setPrice(tradeSaveDTO.getAmount());
        User publisher = tradePost.getPublisherId() == null ? null : userMapper.selectById(tradePost.getPublisherId());

        String resolvedContactName = StrUtil.firstNonBlank(
                StrUtil.trim(tradeSaveDTO.getClientName()),
                buildDisplayName(publisher),
                tradePost.getContactName()
        );
        String resolvedContactPhone = StrUtil.firstNonBlank(
                StrUtil.trim(publisher == null ? null : publisher.getPhone()),
                StrUtil.trim(tradeSaveDTO.getClientPhone()),
                StrUtil.trim(tradePost.getContactPhone())
        );

        if (StrUtil.isBlank(resolvedContactPhone)) {
            throw new RuntimeException("委托人联系电话不能为空");
        }

        tradePost.setContactName(resolvedContactName);
        tradePost.setContactPhone(resolvedContactPhone);
    }

    private void saveReviewRecord(Long postId, Long reviewerId, Integer reviewResult, String reviewRemark) {
        TradePostReview review = new TradePostReview();
        review.setPostId(postId);
        review.setReviewerId(reviewerId);
        review.setReviewResult(reviewResult);
        review.setReviewRemark(reviewRemark);
        tradePostReviewMapper.insertTradePostReview(review);
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

    private TradeQueryDTO normalizeTradeQuery(TradeQueryDTO queryDTO) {
        TradeQueryDTO normalized = new TradeQueryDTO();
        if (queryDTO == null) {
            normalized.setPageNum(1);
            normalized.setPageSize(10);
            return normalized;
        }
        normalized.setPageNum(queryDTO.getPageNum() == null || queryDTO.getPageNum() < 1 ? 1 : queryDTO.getPageNum());
        normalized.setPageSize(queryDTO.getPageSize() == null || queryDTO.getPageSize() < 1 ? 10 : queryDTO.getPageSize());
        normalized.setTitle(queryDTO.getTitle());
        normalized.setMinAmount(queryDTO.getMinAmount());
        normalized.setMaxAmount(queryDTO.getMaxAmount());
        normalized.setStartDate(queryDTO.getStartDate());
        normalized.setEndDate(queryDTO.getEndDate());

        Integer status = parseTradeStatus(queryDTO.getStatus());
        normalized.setStatus(status == null ? null : String.valueOf(status));
        return normalized;
    }

    private long offset(Integer pageNum, Integer pageSize) {
        int currentPage = pageNum == null || pageNum < 1 ? 1 : pageNum;
        int currentSize = pageSize == null || pageSize < 1 ? 10 : pageSize;
        return (long) (currentPage - 1) * currentSize;
    }

    private int pageSize(Integer pageSize) {
        return pageSize == null || pageSize < 1 ? 10 : pageSize;
    }

    private String buildDisplayName(User user) {
        return user == null ? null : StrUtil.blankToDefault(user.getNickname(), user.getUsername());
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

    private String generateOrderNo() {
        return "TO" + System.currentTimeMillis();
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
            case "onshelf":
            case "on_shelf":
                return 3;
            case "trading":
                return 4;
            case "offshelf":
            case "off_shelf":
            case "completed":
                return 5;
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
                return "trading";
            case 5:
                return "completed";
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

    private Long currentUserId() {
        return SecurityUtils.requireCurrentUserId();
    }

    private boolean isAdmin() {
        return SecurityUtils.isAdmin();
    }
}
