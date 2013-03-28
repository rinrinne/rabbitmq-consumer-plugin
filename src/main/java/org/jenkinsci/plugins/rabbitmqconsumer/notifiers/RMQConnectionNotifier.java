package org.jenkinsci.plugins.rabbitmqconsumer.notifiers;

import org.jenkinsci.plugins.rabbitmqconsumer.events.RMQConnectionEvent;
import org.jenkinsci.plugins.rabbitmqconsumer.listeners.RMQConnectionListener;

/**
 * Notifier interface for {@link RMQConnectionListener}.
 * 
 * @author rinrinne a.k.a. rin_ne
 */
public interface RMQConnectionNotifier {
    /**
     * Add {@link RMQConnectionListener}.
     * 
     * @param rmqShutdownListener
     *            the listener.
     */
    void addRMQConnectionListener(RMQConnectionListener rmqShutdownListener);

    /**
     * Notify event for {@link RMQConnection}.
     * 
     * @param event
     *            the event.
     */
    void notifyRMQConnectionListeners(RMQConnectionEvent event);

    /**
     * Removes {@link RMQConnectionListener}.
     * 
     * @param rmqShutdownListener
     *            the listener.
     */
    void removeRMQConnectionListener(RMQConnectionListener rmqShutdownListener);

    /**
     * Gets whether connection is opened.
     * 
     * @return true if connection is already opened.
     */
    boolean isOpenRMQConnection();

}
