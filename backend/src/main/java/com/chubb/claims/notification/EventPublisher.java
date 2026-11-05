package com.chubb.claims.notification;

public interface EventPublisher {
    void publish(DomainEvents.ClaimEvent event);
}
