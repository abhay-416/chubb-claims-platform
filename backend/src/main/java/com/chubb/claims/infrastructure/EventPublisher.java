package com.chubb.claims.infrastructure;

public interface EventPublisher {
    void publish(DomainEvents.ClaimEvent event);
}
