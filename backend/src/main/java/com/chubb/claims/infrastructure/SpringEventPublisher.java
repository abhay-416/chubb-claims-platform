package com.chubb.claims.infrastructure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.messaging.provider", havingValue = "spring", matchIfMissing = true)
public class SpringEventPublisher implements EventPublisher {

    private static final Logger log = LoggerFactory.getLogger(SpringEventPublisher.class);
    private final ApplicationEventPublisher applicationEventPublisher;

    public SpringEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public void publish(DomainEvents.ClaimEvent event) {
        log.info("[In-Memory Event Bus] Publishing event: {} for claim {}", event.eventType(), event.claimNumber());
        applicationEventPublisher.publishEvent(event);
    }

    @Async
    @EventListener
    public void handleClaimEvent(DomainEvents.ClaimEvent event) {
        log.info("[In-Memory Event Consumer] Asynchronously processed event: {} | Status: {} -> {} | Market: {}",
            event.eventType(), event.oldStatus(), event.newStatus(), event.market());
    }
}
