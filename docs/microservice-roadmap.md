# 微服务化演进预留说明

当前项目仍然以单体方式运行，但已经按“可拆分”的方向进行了预留：

## 推荐拆分边界

1. `auth-service`
负责登录、JWT、用户认证、角色鉴权。

2. `user-service`
负责用户资料、角色、后台用户管理。

3. `trade-service`
负责发布、审核、订单、交易流程。

4. `chat-service`
负责会话、消息、未读数、聊天记录。

5. `gateway-service`
后续如果接入 Spring Cloud Gateway，可统一鉴权、路由、限流。

## 当前已经完成的预留

- Spring Security + JWT 已经独立成安全层。
- 交易、角色、聊天已经分成相对清晰的领域服务。
- 新增了 `EventPublisher` 抽象，后续可直接接入 MQ。

## 推荐事件主题

- `trade.post.created`
- `trade.post.updated`
- `trade.post.approved`
- `trade.post.rejected`
- `trade.order.received`
- `trade.order.completed`
- `trade.order.cancelled`

## 后续如需真正拆成微服务

1. 先抽出公共模块：统一返回体、异常、JWT 工具、DTO/VO 协议。
2. 将 `auth`、`trade`、`chat` 拆成独立 Maven 模块或独立仓库。
3. 接入注册中心、配置中心、网关。
4. 将 `EventPublisher` 的默认实现替换为 MQ 实现。
5. 将服务间同步调用替换为 OpenFeign 或 HTTP Client。
