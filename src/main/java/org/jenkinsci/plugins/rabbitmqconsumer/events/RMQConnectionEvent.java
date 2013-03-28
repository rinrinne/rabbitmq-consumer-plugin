package org.jenkinsci.plugins.rabbitmqconsumer.events;

/**
 * Events for {@link RMQConnectionListener}.
 * 
 * @author nobuhiro
 */
public enum RMQConnectionEvent {
    /**
     * OnOpen event.
     */
    OPEN,
    /**
     * OnCloseCompleted event.
     */
    CLOSE_COMPLETED;
}
