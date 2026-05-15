package com.example.admin.service.impl;

import cn.hutool.core.util.StrUtil;
import com.example.admin.common.PageResult;
import com.example.admin.common.assembler.UserProfileAssembler;
import com.example.admin.common.event.EventPublisher;
import com.example.admin.dto.TradeQueryDTO;
import com.example.admin.dto.TradeReviewDTO;
import com.example.admin.dto.TradeSaveDTO;
import com.example.admin.entity.TradeCategory;
import com.example.admin.entity.TradeOrder;
import com.example.admin.entity.TradePostCategory;
import com.example.admin.entity.TradePost;
import com.example.admin.entity.TradePostImage;
import com.example.admin.entity.TradePostReview;
import com.example.admin.entity.User;
import com.example.admin.entity.UserProfile;
import com.example.admin.entity.Qualification;
import com.example.admin.mapper.QualificationMapper;
import com.example.admin.mapper.TradeCategoryMapper;
import com.example.admin.mapper.TradeOrderMapper;
import com.example.admin.mapper.TradePostCategoryMapper;
import com.example.admin.mapper.TradePostImageMapper;
import com.example.admin.mapper.TradePostMapper;
import com.example.admin.mapper.TradePostReviewMapper;
import com.example.admin.mapper.UserMapper;
import com.example.admin.mapper.UserProfileMapper;
import com.example.admin.security.SecurityUtils;
import com.example.admin.service.TradeService;
import com.example.admin.vo.TradeCategoryVO;
import com.example.admin.vo.TradeOrderStatsVO;
import com.example.admin.vo.TradeOrderVO;
import com.example.admin.vo.TradeVO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * 交易模块服务实现
 */
@Service
public class TradeServiceImpl implements TradeService {

    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String PAYMENT_GATEWAY_MOCK = "mock";
    private static final String PAYMENT_GATEWAY_WECHAT = "wechat";
    private static final int PAY_STATUS_UNPAID = 0;
    private static final int PAY_STATUS_PAID = 1;
    private static final int PAY_STATUS_REFUNDED = 2;
    private static final int PAY_STATUS_SETTLED = 3;
    private static final BigDecimal DEFAULT_WALLET_BALANCE = new BigDecimal("100000.00");

    @Resource
    private TradePostMapper tradePostMapper;

    @Resource
    private TradeOrderMapper tradeOrderMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private UserProfileMapper userProfileMapper;

    @Resource
    private TradePostReviewMapper tradePostReviewMapper;

    @Resource
    private TradePostImageMapper tradePostImageMapper;

    @Resource
    private TradeCategoryMapper tradeCategoryMapper;

    @Resource
    private TradePostCategoryMapper tradePostCategoryMapper;

    @Resource
    private QualificationMapper qualificationMapper;

    @Resource
    private EventPublisher eventPublisher;

    @Resource
    private UserProfileAssembler userProfileAssembler;

    @Value("${payment.gateway:mock}")
    private String paymentGateway;

    @Override
    public PageResult<List<TradeVO>> getPublishPage(TradeQueryDTO queryDTO) {
        TradeQueryDTO normalizedQuery = normalizeTradeQuery(queryDTO);
        if (StrUtil.isBlank(normalizedQuery.getStatus())) {
            normalizedQuery.setStatus("all");
            normalizedQuery.setStatusList(null);
        } else if ("all".equalsIgnoreCase(normalizedQuery.getStatus())) {
            normalizedQuery.setStatusList(null);
        }
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
            saveTradePostImages(tradePost.getId(), tradeSaveDTO.getImageUrls());
            saveTradePostCategories(tradePost.getId(), tradeSaveDTO.getCategoryNames());
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
            saveTradePostImages(tradePost.getId(), tradeSaveDTO.getImageUrls());
            saveTradePostCategories(tradePost.getId(), tradeSaveDTO.getCategoryNames());
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
            tradePostImageMapper.deleteByPostId(id);
            tradePostCategoryMapper.deleteByPostId(id);
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
    public List<TradeCategoryVO> getAvailableTradeCategories() {
        return tradeCategoryMapper.selectEnabledList().stream()
                .map(item -> TradeCategoryVO.builder()
                        .id(item.getId())
                        .categoryName(item.getCategoryName())
                        .requiresQualification(item.getRequiresQualification() != null && item.getRequiresQualification() == 1)
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public boolean receiveTradePost(Long postId) {
        Long currentUserId = currentUserId();
        TradePost tradePost = getTradePostById(postId);
        assertQualifiedReceiver(currentUserId, tradePost);
        if (tradePost.getStatus() == null || tradePost.getStatus() != 3) {
            throw new RuntimeException("当前交易不可接取");
        }
        if (!isAdmin() && currentUserId.equals(tradePost.getPublisherId())) {
            throw new AccessDeniedException("不能接取自己发布的交易");
        }

        TradeOrder latestOrder = tradeOrderMapper.selectLatestByPostId(postId);
        TradeOrder order;
        boolean createOrder = latestOrder == null || latestOrder.getStatus() == null || latestOrder.getStatus() == 4;
        if (createOrder) {
            order = new TradeOrder();
            order.setOrderNo(generateOrderNo());
            order.setPostId(tradePost.getId());
            order.setPublisherId(tradePost.getPublisherId());
            order.setAmount(tradePost.getPrice());
            order.setRemark("用户从交易大全接取交易");
        } else {
            if (latestOrder.getStatus() == 0 || latestOrder.getStatus() == 1 || latestOrder.getStatus() == 2 || latestOrder.getStatus() == 3) {
                throw new RuntimeException("当前交易已被接取");
            }
            order = latestOrder;
        }

        order.setReceiverId(currentUserId);
        order.setStatus(0);
        order.setPayStatus(PAY_STATUS_UNPAID);
        order.setPayGateway(null);
        order.setPayNo(null);
        order.setPayTime(null);
        order.setRefundTime(null);
        order.setConfirmTime(null);
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
                .successCount(Math.toIntExact(tradeOrderMapper.countPage(3, currentUserId, admin)))
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
        TradePost qualificationTradePost = tradePostMapper.selectById(order.getPostId());
        assertQualifiedReceiver(currentUserId, qualificationTradePost);
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
    @Transactional(rollbackFor = Exception.class)
    public boolean completeOrder(Long id) {
        TradeOrder order = getTradeOrderById(id);
        assertOrderAccessible(order);
        Long currentUserId = currentUserId();
        if (!currentUserId.equals(order.getReceiverId())) {
            throw new AccessDeniedException("Only receiver can submit completion");
        }
        if (order.getStatus() != 1) {
            throw new RuntimeException("Only in-progress orders can be completed");
        }

        order.setStatus(2);
        order.setFinishTime(LocalDateTime.now());
        boolean success = tradeOrderMapper.updateTradeOrder(order) > 0;
        if (success) {
            eventPublisher.publish("trade.order.receiver.completed", order.getOrderNo());
        }
        return success;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean confirmOrder(Long id) {
        TradeOrder order = getTradeOrderById(id);
        assertOrderAccessible(order);
        Long currentUserId = currentUserId();
        if (!currentUserId.equals(order.getPublisherId())) {
            throw new AccessDeniedException("Only publisher can confirm completion");
        }
        if (order.getStatus() != 2) {
            throw new RuntimeException("Order is not waiting for publisher confirmation");
        }

        order.setStatus(3);
        executeGatewayPay(order);
        BigDecimal amount = safeAmount(order.getAmount());
        decreaseWalletBalance(order.getPublisherId(), amount);
        increaseWalletBalance(order.getReceiverId(), amount);

        order.setPayStatus(PAY_STATUS_SETTLED);
        order.setPayGateway(resolvePaymentGateway());
        order.setPayNo(generateMockPayNo(order.getId()));
        order.setPayTime(LocalDateTime.now());
        order.setRefundTime(null);
        boolean updated = tradeOrderMapper.updateTradeOrder(order) > 0;

        TradePost tradePost = tradePostMapper.selectById(order.getPostId());
        if (updated && tradePost != null) {
            tradePost.setStatus(5);
            tradePost.setOffShelfTime(LocalDateTime.now());
            tradePostMapper.updateTradePost(tradePost);
        }
        if (updated) {
            eventPublisher.publish("trade.order.confirmed.completed", order.getOrderNo());
        }
        return updated;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean payOrder(Long id) {
        TradeOrder order = getTradeOrderById(id);
        assertOrderAccessible(order);
        Long currentUserId = currentUserId();

        if (!currentUserId.equals(order.getPublisherId())) {
            throw new AccessDeniedException("Only publisher can pay order");
        }

        if (order.getStatus() != 3) {
            throw new RuntimeException("Only fully confirmed completed orders can be paid");
        }

        if (!isPayUnpaid(order.getPayStatus())) {
            throw new RuntimeException("Order has already been paid or settled");
        }

        executeGatewayPay(order);
        BigDecimal amount = safeAmount(order.getAmount());
        decreaseWalletBalance(order.getPublisherId(), amount);
        increaseWalletBalance(order.getReceiverId(), amount);

        order.setPayStatus(PAY_STATUS_SETTLED);
        order.setPayGateway(resolvePaymentGateway());
        order.setPayNo(generateMockPayNo(order.getId()));
        order.setPayTime(LocalDateTime.now());
        order.setRefundTime(null);

        boolean success = tradeOrderMapper.updateTradeOrder(order) > 0;
        if (success) {
            eventPublisher.publish("trade.order.paid", order.getOrderNo());
        }
        return success;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelOrder(Long id) {
        TradeOrder order = getTradeOrderById(id);
        assertOrderAccessible(order);
        Long currentUserId = currentUserId();
        if (!currentUserId.equals(order.getReceiverId())) {
            throw new AccessDeniedException("Only receiver can cancel order");
        }
        if (order.getStatus() == 3) {
            throw new RuntimeException("Completed orders cannot be cancelled");
        }

        order.setStatus(4);
        order.setCancelTime(LocalDateTime.now());
        if (StrUtil.isBlank(order.getCancelReason())) {
            order.setCancelReason("Receiver cancelled order");
        }
        if (isPayPaid(order.getPayStatus()) || isPaySettled(order.getPayStatus())) {
            increaseWalletBalance(order.getPublisherId(), safeAmount(order.getAmount()));
            order.setPayGateway(null);
            order.setPayNo(null);
            order.setPayTime(null);
            order.setPayStatus(PAY_STATUS_REFUNDED);
            order.setRefundTime(LocalDateTime.now());
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
        List<String> imageUrls = getTradePostImageUrls(tradePost.getId());
        String tradeStatus = formatTradeStatus(tradePost.getStatus());
        String tradeStatusText = formatTradeStatusText(tradePost.getStatus());
        String orderStatus = relatedOrder == null ? null : formatOrderStatus(relatedOrder.getStatus());
        String orderStatusText = relatedOrder == null ? null : formatOrderStatusText(relatedOrder.getStatus());
        String effectiveFlowStatus = orderStatus;
        String effectiveFlowStatusText = orderStatusText;
        if (relatedOrder != null && relatedOrder.getStatus() != null && relatedOrder.getStatus() == 3 && isPayUnpaid(relatedOrder.getPayStatus())) {
            effectiveFlowStatus = "pay_pending";
            effectiveFlowStatusText = "待支付";
        }
        boolean useOrderStatus = relatedOrder != null && relatedOrder.getStatus() != null && relatedOrder.getStatus() != 4;
        String flowStatus = useOrderStatus ? effectiveFlowStatus : tradeStatus;
        String flowStatusText = useOrderStatus ? effectiveFlowStatusText : tradeStatusText;

        return TradeVO.builder()
                .id(tradePost.getId())
                .postNo(tradePost.getPostNo())
                .orderId(relatedOrder == null ? null : relatedOrder.getId())
                .orderNo(relatedOrder == null ? null : relatedOrder.getOrderNo())
                .title(tradePost.getTitle())
                .clientName(buildDisplayName(publisher))
                .clientPhone(tradePost.getContactPhone())
                .workerName(receiver == null ? null : buildDisplayName(receiver))
                .workerPhone(receiver == null ? null : receiver.getPhone())
                .publisher(userProfileAssembler.toProfile(publisher))
                .worker(userProfileAssembler.toProfile(receiver))
                .amount(tradePost.getPrice())
                .location(buildLocation(tradePost))
                .cityName(tradePost.getCityName())
                .areaName(tradePost.getAreaName())
                .longitude(tradePost.getLongitude())
                .latitude(tradePost.getLatitude())
                .status(tradeStatus)
                .statusText(tradeStatusText)
                .flowStatus(flowStatus)
                .flowStatusText(flowStatusText)
                .orderStatus(orderStatus)
                .orderStatusText(orderStatusText)
                .createTime(formatDateTime(tradePost.getCreateTime()))
                .description(tradePost.getContent())
                .imageUrls(imageUrls)
                .categoryNames(parseCategoryNames(tradePost.getCategoryNamesText()))
                .build();
    }

    private TradeOrderVO toTradeOrderVO(TradeOrder order) {
        TradePost tradePost = tradePostMapper.selectById(order.getPostId());
        User publisher = userMapper.selectById(order.getPublisherId());
        User receiver = order.getReceiverId() == null ? null : userMapper.selectById(order.getReceiverId());
        String title = tradePost == null ? "" : tradePost.getTitle();
        String area = buildLocation(tradePost);
        List<String> imageUrls = tradePost == null ? Collections.emptyList() : getTradePostImageUrls(tradePost.getId());

        return TradeOrderVO.builder()
                .id(order.getId())
                .postId(order.getPostId())
                .orderNo(order.getOrderNo())
                .title(title)
                .area(area)
                .publisher(userProfileAssembler.toProfile(publisher))
                .receiver(userProfileAssembler.toProfile(receiver))
                .createTime(formatDateTime(order.getCreateTime()))
                .price(order.getAmount())
                .status(formatOrderStatus(order.getStatus()))
                .statusText(formatOrderStatusText(order.getStatus()))
                .payStatus(formatPayStatus(order.getPayStatus()))
                .payStatusText(formatPayStatusText(order.getPayStatus()))
                .payGateway(order.getPayGateway())
                .payTime(formatDateTime(order.getPayTime()))
                .imageUrls(imageUrls)
                .categoryNames(parseCategoryNames(tradePost.getCategoryNamesText()))
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

    private void assertQualifiedReceiver(Long userId, TradePost tradePost) {
        if (isAdmin()) {
            return;
        }

        List<String> requiredQualificationTypes = resolveRequiredQualificationTypes(tradePost);
        if (requiredQualificationTypes.isEmpty()) {
            return;
        }

        List<String> approvedQualificationTypes = qualificationMapper.selectApprovedQualificationTypesByUserId(userId);
        List<String> normalizedApprovedTypes = approvedQualificationTypes == null
                ? Collections.emptyList()
                : approvedQualificationTypes.stream()
                    .filter(StrUtil::isNotBlank)
                    .map(item -> item.trim().toLowerCase(Locale.ROOT))
                    .collect(Collectors.toList());

        boolean matched = requiredQualificationTypes.stream()
                .map(item -> item.toLowerCase(Locale.ROOT))
                .anyMatch(normalizedApprovedTypes::contains);

        if (!matched) {
            throw new AccessDeniedException("Current trade category requires approved qualification before receiving orders");
        }
    }

    private List<String> resolveRequiredQualificationTypes(TradePost tradePost) {
        List<String> categoryNames = parseCategoryNames(tradePost == null ? null : tradePost.getCategoryNamesText());
        if (categoryNames.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> requiredTypes = new ArrayList<>();
        for (String categoryName : categoryNames) {
            if (StrUtil.isBlank(categoryName)) {
                continue;
            }

            TradeCategory category = tradeCategoryMapper.selectByName(categoryName.trim());
            if (category != null && category.getRequiresQualification() != null && category.getRequiresQualification() == 1) {
                requiredTypes.add(category.getCategoryName());
            }
        }
        return requiredTypes;
    }

    private void fillTradePost(TradePost tradePost, TradeSaveDTO tradeSaveDTO) {
        tradePost.setTitle(tradeSaveDTO.getTitle());
        tradePost.setContent(tradeSaveDTO.getDescription());
        tradePost.setPrice(tradeSaveDTO.getAmount());
        tradePost.setCityName(StrUtil.emptyToNull(StrUtil.trim(tradeSaveDTO.getCityName())));
        tradePost.setAreaName(StrUtil.emptyToNull(StrUtil.trim(tradeSaveDTO.getAreaName())));
        tradePost.setAddress(StrUtil.emptyToNull(StrUtil.trim(tradeSaveDTO.getLocation())));
        tradePost.setLongitude(tradeSaveDTO.getLongitude());
        tradePost.setLatitude(tradeSaveDTO.getLatitude());
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

    private void saveTradePostImages(Long postId, List<String> imageUrls) {
        if (postId == null) {
            return;
        }

        tradePostImageMapper.deleteByPostId(postId);

        List<String> normalizedUrls = normalizeImageUrls(imageUrls);
        for (int i = 0; i < normalizedUrls.size(); i++) {
            TradePostImage image = new TradePostImage();
            image.setPostId(postId);
            image.setImageUrl(normalizedUrls.get(i));
            image.setSortNo(i + 1);
            tradePostImageMapper.insertTradePostImage(image);
        }
    }

    private void saveTradePostCategories(Long postId, List<String> categoryNames) {
        if (postId == null) {
            return;
        }

        tradePostCategoryMapper.deleteByPostId(postId);

        List<String> normalizedNames = normalizeCategoryNames(categoryNames);
        for (int i = 0; i < normalizedNames.size(); i++) {
            String categoryName = normalizedNames.get(i);
            TradeCategory category = tradeCategoryMapper.selectByName(categoryName);
            if (category == null) {
                category = new TradeCategory();
                category.setCategoryName(categoryName);
                category.setStatus(1);
                tradeCategoryMapper.insertTradeCategory(category);
            }

            TradePostCategory relation = new TradePostCategory();
            relation.setPostId(postId);
            relation.setCategoryId(category.getId());
            relation.setSortNo(i + 1);
            tradePostCategoryMapper.insertTradePostCategory(relation);
        }
    }

    private List<String> normalizeCategoryNames(List<String> categoryNames) {
        if (categoryNames == null || categoryNames.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> normalized = new ArrayList<>();
        for (String item : categoryNames) {
            String value = StrUtil.emptyToNull(StrUtil.trim(item));
            if (value != null && !normalized.contains(value)) {
                normalized.add(value);
            }
        }
        return normalized;
    }

    private List<String> parseCategoryNames(String categoryNamesText) {
        if (StrUtil.isBlank(categoryNamesText)) {
            return Collections.emptyList();
        }
        return StrUtil.splitTrim(categoryNamesText, ',');
    }

    private List<String> getTradePostImageUrls(Long postId) {
        if (postId == null) {
            return Collections.emptyList();
        }

        return tradePostImageMapper.selectByPostId(postId).stream()
                .map(TradePostImage::getImageUrl)
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toList());
    }

    private List<String> normalizeImageUrls(List<String> imageUrls) {
        if (imageUrls == null || imageUrls.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> normalizedUrls = new ArrayList<>();
        for (String imageUrl : imageUrls) {
            String normalized = StrUtil.emptyToNull(StrUtil.trim(imageUrl));
            if (normalized != null && !normalizedUrls.contains(normalized)) {
                normalizedUrls.add(normalized);
            }
        }
        return normalizedUrls;
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
        normalized.setCategoryNames(queryDTO.getCategoryNames());
        normalized.setUserCityName(queryDTO.getUserCityName());
        normalized.setUserAreaName(queryDTO.getUserAreaName());
        normalized.setUserLongitude(queryDTO.getUserLongitude());
        normalized.setUserLatitude(queryDTO.getUserLatitude());

        normalized.setStatus(queryDTO.getStatus());
        normalized.setStatusList(resolveTradeStatusList(queryDTO.getStatus()));
        return normalized;
    }

    private List<Integer> resolveTradeStatusList(String status) {
        if (StrUtil.isBlank(status) || "published".equalsIgnoreCase(status)) {
            return Collections.singletonList(3);
        }
        if ("all".equalsIgnoreCase(status)) {
            List<Integer> list = new ArrayList<>();
            list.add(3);
            list.add(5);
            return list;
        }
        if ("completed".equalsIgnoreCase(status)) {
            return Collections.singletonList(5);
        }
        Integer parsed = parseTradeStatus(status);
        return parsed == null ? Collections.singletonList(3) : Collections.singletonList(parsed);
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

    private String buildLocation(TradePost tradePost) {
        if (tradePost == null) {
            return "";
        }
        if (StrUtil.isNotBlank(tradePost.getAddress())) {
            return tradePost.getAddress();
        }
        return buildArea(tradePost.getCityName(), tradePost.getAreaName());
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

    private String formatTradeStatusText(Integer status) {
        if (status == null) {
            return "";
        }
        switch (status) {
            case 0:
                return "草稿";
            case 1:
                return "审核中";
            case 2:
                return "驳回";
            case 3:
                return "发布中";
            case 4:
                return "进行中";
            case 5:
                return "已完成";
            default:
                return "未知状态";
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
            case "confirm_pending":
            case "confirming":
                return 2;
            case "success":
            case "completed":
                return 3;
            case "cancel":
            case "cancelled":
                return 4;
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
                return "confirm_pending";
            case 3:
                return "success";
            case 4:
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
                return "待委托方确认";
            case 3:
                return "已完成";
            case 4:
                return "已取消";
            default:
                return "未知状态";
        }
    }

    private String formatPayStatus(Integer payStatus) {
        if (payStatus == null) {
            return "unpaid";
        }
        switch (payStatus) {
            case PAY_STATUS_UNPAID:
                return "unpaid";
            case PAY_STATUS_PAID:
                return "paid";
            case PAY_STATUS_REFUNDED:
                return "refunded";
            case PAY_STATUS_SETTLED:
                return "settled";
            default:
                return String.valueOf(payStatus);
        }
    }

    private String formatPayStatusText(Integer payStatus) {
        if (payStatus == null) {
            return "待支付";
        }
        switch (payStatus) {
            case PAY_STATUS_UNPAID:
                return "待支付";
            case PAY_STATUS_PAID:
                return "已支付";
            case PAY_STATUS_REFUNDED:
                return "已退款";
            case PAY_STATUS_SETTLED:
                return "已结算";
            default:
                return "未知支付状态";
        }
    }

    private boolean isPayUnpaid(Integer payStatus) {
        return payStatus == null || payStatus == PAY_STATUS_UNPAID;
    }

    private boolean isPayPaid(Integer payStatus) {
        return payStatus != null && payStatus == PAY_STATUS_PAID;
    }

    private boolean isPaySettled(Integer payStatus) {
        return payStatus != null && payStatus == PAY_STATUS_SETTLED;
    }

    private String resolvePaymentGateway() {
        String normalized = StrUtil.blankToDefault(paymentGateway, PAYMENT_GATEWAY_MOCK).trim().toLowerCase(Locale.ROOT);
        if (PAYMENT_GATEWAY_MOCK.equals(normalized) || PAYMENT_GATEWAY_WECHAT.equals(normalized)) {
            return normalized;
        }
        return PAYMENT_GATEWAY_MOCK;
    }

    private void executeGatewayPay(TradeOrder order) {
        String gateway = resolvePaymentGateway();
        if (PAYMENT_GATEWAY_MOCK.equals(gateway)) {
            return;
        }
        if (PAYMENT_GATEWAY_WECHAT.equals(gateway)) {
            throw new RuntimeException("微信支付网关暂未接入，请先切换到 mock 网关");
        }
        throw new RuntimeException("暂不支持的支付网关：" + gateway);
    }

    private String generateMockPayNo(Long orderId) {
        String suffix = orderId == null ? "0" : String.valueOf(orderId);
        return "MOCKPAY-" + suffix + "-" + System.currentTimeMillis();
    }

    private BigDecimal safeAmount(BigDecimal amount) {
        if (amount == null || amount.signum() < 0) {
            return BigDecimal.ZERO;
        }
        return amount;
    }

    private void decreaseWalletBalance(Long userId, BigDecimal amount) {
        if (userId == null) {
            throw new RuntimeException("支付用户不存在");
        }
        if (amount == null || amount.signum() <= 0) {
            return;
        }
        UserProfile profile = ensureUserProfile(userId);
        BigDecimal balance = profile.getWalletBalance() == null ? BigDecimal.ZERO : profile.getWalletBalance();
        if (balance.compareTo(amount) < 0) {
            throw new RuntimeException("钱包余额不足，请先充值后再支付");
        }
        int updated = userProfileMapper.increaseWalletBalance(userId, amount.negate());
        if (updated <= 0) {
            throw new RuntimeException("钱包扣款失败，请稍后重试");
        }
    }

    private void increaseWalletBalance(Long userId, BigDecimal amount) {
        if (userId == null) {
            throw new RuntimeException("钱包用户不存在");
        }
        if (amount == null || amount.signum() <= 0) {
            return;
        }
        ensureUserProfile(userId);
        int updated = userProfileMapper.increaseWalletBalance(userId, amount);
        if (updated <= 0) {
            throw new RuntimeException("钱包入账失败，请稍后重试");
        }
    }

    private UserProfile ensureUserProfile(Long userId) {
        UserProfile profile = userProfileMapper.selectByUserId(userId);
        if (profile != null) {
            return profile;
        }
        UserProfile nextProfile = new UserProfile();
        nextProfile.setUserId(userId);
        nextProfile.setGender(0);
        nextProfile.setWalletBalance(DEFAULT_WALLET_BALANCE);
        userProfileMapper.insertUserProfile(nextProfile);
        UserProfile created = userProfileMapper.selectByUserId(userId);
        if (created == null) {
            throw new RuntimeException("用户钱包初始化失败");
        }
        return created;
    }

    private Long currentUserId() {
        return SecurityUtils.requireCurrentUserId();
    }

    private boolean isAdmin() {
        return SecurityUtils.isAdmin();
    }
}
