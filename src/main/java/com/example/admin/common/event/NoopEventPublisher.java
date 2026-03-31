package com.example.admin.common.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 默认事件发布器
 */
@Slf4j
@Component
public class NoopEventPublisher implements EventPublisher {

    @Override
    public void publish(String topic, Object payload) {
        log.debug("Event published to topic [{}]: {}", topic, payload);
    }
}
