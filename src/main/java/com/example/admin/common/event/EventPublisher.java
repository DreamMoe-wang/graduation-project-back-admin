package com.example.admin.common.event;

/**
 * 领域事件发布器
 *
 * <p>当前默认实现为本地空实现，后续可无缝替换为 MQ、消息总线或远程事件网关。</p>
 */
public interface EventPublisher {

    /**
     * 发布事件
     */
    void publish(String topic, Object payload);
}
