package org.jenkinsci.plugins.rabbitmqconsumer.notifiers;

import org.jenkinsci.plugins.rabbitmqconsumer.events.RMQChannelEvent;
import org.jenkinsci.plugins.rabbitmqconsumer.listeners.RMQChannelListener;

/**
 * Notifier interface for {@link RMQChannel}.
 * 
 * @author rinrinne a.k.a. rin_ne
 */
public interface RMQChannelNotifier {

    /**
     * Add {@link RMQChannelListener}.
     * 
     * @param rmqChannelListener
     *            the listener.
     */
    void addRMQChannelListener(RMQChannelListener rmqChannelListener);

    /**
     * Notify {@link RMQChannelListener}s.
     * 
     * @param event
     *            the event.
     */
    void notifyRMQChannelListeners(RMQChannelEvent event);

    /**
     * Removes {@link RMQChannelListener}.
     * 
     * @param rmqChannelListener
     *            the listener.
     */
    void removeRMQChannelListener(RMQChannelListener rmqChannelListener);

    /**
     * Gets whether channel is opened.
     * 
     * @return true if channel is already opened.
     */
    boolean isOpenRMQChannel();
}
