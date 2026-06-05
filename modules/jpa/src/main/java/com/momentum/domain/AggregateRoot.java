package com.momentum.domain;

import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.AfterDomainEventPublication;
import org.springframework.data.domain.DomainEvents;

@MappedSuperclass
public abstract class AggregateRoot extends BaseEntity {

  @Transient
  private final transient List<Object> domainEvents = new ArrayList<>();

  protected void registerEvent(Object event) {
    domainEvents.add(event);
  }

  @DomainEvents
  public Collection<Object> domainEvents() {
    return List.copyOf(domainEvents);
  }

  @AfterDomainEventPublication
  protected void clearDomainEvents() {
    domainEvents.clear();
  }
}
